package com.example.gunpo.service;

import com.example.gunpo.domain.SmokingArea;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class SmokingZoneService {

    @Value("${service.key}")
    private String serviceKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    public List<SmokingArea> getDataFromAPI(int page, int size) {
        String url = buildApiUrl(page, size);
        log.info("요청 URL: {}", url);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(new URI(url), String.class);
            log.info("응답 상태 코드: {}", responseEntity.getStatusCode());

            return handleApiResponse(responseEntity);
        } catch (URISyntaxException e) {
            log.error("URI 문법 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("API 호출 중 오류가 발생했습니다: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    // 모든 데이터를 가져오고 Redis에 저장하는 메서드
    public void fetchAllDataAndSaveToRedis() {
        List<SmokingArea> allSmokingAreas = new ArrayList<>();
        int page = 1;
        int size = 100; // 페이지당 데이터 수 조정

        while (true) {
            List<SmokingArea> smokingAreas = getDataFromAPI(page, size);
            if (smokingAreas.isEmpty()) {
                log.info("더 이상 흡연 구역 데이터가 없습니다. 데이터 수집 종료.");
                break; // 데이터가 없으면 종료
            }
            allSmokingAreas.addAll(smokingAreas); // 수집된 데이터에 추가
            page++;
        }

        // Redis에 모든 데이터 저장
        if (!allSmokingAreas.isEmpty()) {
            redisService.saveToRedis(allSmokingAreas); // RedisService에서 중복 검증 및 저장 처리
            log.info("모든 흡연 구역 데이터가 Redis에 저장되었습니다.");
        } else {
            log.warn("저장할 흡연 구역 데이터가 없습니다.");
        }
    }

    private String buildApiUrl(int page, int size) {
        String encodedServiceKey = serviceKey.replace("+", "%2B");
        return String.format("https://api.odcloud.kr/api/15122343/v1/uddi:20726a37-6b6a-4b28-8370-9097991eca6a?returnType=json&serviceKey=%s&page=%d&size=%d",
                encodedServiceKey, page, size);
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
        try {
            log.info("원본 응답: {}", response); // 응답 본문 로그 추가
            Map<String, Object> map = objectMapper.readValue(response, Map.class);
            log.info("파싱된 맵: {}", map); // 파싱된 결과 로그 추가

            // "data" 키로 흡연 구역 정보를 가져오기
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) map.get("data");
            if (itemsList != null) {
                return extractSmokingAreas(itemsList); // itemsList를 전달
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
        SmokingArea smokingArea = new SmokingArea();
        smokingArea.setLongitude((String) itemMap.get("경도"));
        smokingArea.setManagementAgency((String) itemMap.get("관리기관"));
        smokingArea.setDataDate((String) itemMap.get("데이터기준일자"));
        smokingArea.setLocation((String) itemMap.get("소재지"));
        smokingArea.setLatitude((String) itemMap.get("위도"));
        smokingArea.setBoothName((String) itemMap.get("흡연부스명"));
        return smokingArea;
    }
}