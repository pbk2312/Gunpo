package com.example.gunpo.service;

import com.example.gunpo.domain.SmokingArea;
import com.example.gunpo.dto.SmokingAreaDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class SmokingAreaService {

    @Value("${service.key}")
    private String serviceKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    public void fetchAllDataAndSaveToRedis() {
        int page = 1;
        int size = 100; // 페이지당 데이터 수 조정

        while (true) {
            List<SmokingArea> smokingAreas = getDataFromAPI(page, size);
            if (smokingAreas.isEmpty()) {
                log.info("더 이상 흡연 구역 데이터가 없습니다. 데이터 수집 종료.");
                break; // 데이터가 없으면 종료
            }
            // Redis에 데이터 저장
            redisService.saveToRedis(smokingAreas);
            log.info("{} 페이지의 흡연 구역 데이터가 Redis에 저장되었습니다.", page);
            page++;
        }
    }

    public List<SmokingArea> getDataFromAPI(int page, int size) {
        String url = buildApiUrl(page, size);
        log.info("요청 URL: {}", url);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(new URI(url), String.class);
            log.info("응답 상태 코드: {}", responseEntity.getStatusCode());
            return handleApiResponse(responseEntity);
        } catch (URISyntaxException e) {
            log.error("URI 문법 오류: {}", e.getMessage());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("API 호출 중 오류 발생: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("API 호출 중 알 수 없는 오류가 발생했습니다: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    private List<SmokingArea> handleApiResponse(ResponseEntity<String> responseEntity) {
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseBody = responseEntity.getBody();
            return parseResponse(responseBody);
        } else {
            log.warn("API 호출이 실패했습니다. 상태 코드: {}", responseEntity.getStatusCodeValue());
            return Collections.emptyList();
        }
    }

    private List<SmokingArea> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            log.warn("응답이 null이거나 비어 있습니다.");
            return Collections.emptyList();
        }

        try {
            log.info("원본 응답: {}", response);
            Map<String, Object> map = objectMapper.readValue(response, Map.class);
            log.info("파싱된 맵: {}", map);

            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) map.get("data");
            if (itemsList != null) {
                return extractSmokingAreas(itemsList);
            } else {
                log.warn("응답에서 흡연 구역 데이터가 없습니다.");
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 오류가 발생했습니다: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<SmokingArea> extractSmokingAreas(List<Map<String, Object>> itemsList) {
        return itemsList.stream()
                .map(this::mapToSmokingArea)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private SmokingArea mapToSmokingArea(Map<String, Object> itemMap) {
        if (itemMap == null) {
            log.warn("itemMap이 null입니다.");
            return null;
        }

        SmokingArea smokingArea = new SmokingArea();
        smokingArea.setLongitude((String) itemMap.get("경도"));
        smokingArea.setManagementAgency((String) itemMap.get("관리기관"));
        smokingArea.setDataDate((String) itemMap.get("데이터기준일자"));
        smokingArea.setLocation((String) itemMap.get("소재지"));
        smokingArea.setLatitude((String) itemMap.get("위도"));
        smokingArea.setBoothName((String) itemMap.get("흡연부스명"));
        return smokingArea;
    }

    private String buildApiUrl(int page, int size) {
        String encodedServiceKey = serviceKey.replace("+", "%2B");
        return String.format("https://api.odcloud.kr/api/15122343/v1/uddi:20726a37-6b6a-4b28-8370-9097991eca6a?returnType=json&serviceKey=%s&page=%d&size=%d",
                encodedServiceKey, page, size);
    }

    public List<SmokingAreaDto> getAllSmokingZones(){
        return redisService.getAllSmokingZonesFromRedis();
    }


}