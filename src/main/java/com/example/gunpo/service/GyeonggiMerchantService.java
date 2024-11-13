package com.example.gunpo.service;

import com.example.gunpo.dto.RegionMnyFacltStusDto;
import com.example.gunpo.service.redis.RedisGyeonggiMerchantService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
public class GyeonggiMerchantService {

    @Value("${gyeonggi.currency.data.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisGyeonggiMerchantService redisService;

    public void fetchAllDataAndSaveToRedis() {
        int page = 1;
        int size = 10;

        while (true) {
            List<RegionMnyFacltStusDto> merchants = getDataFromAPI(page, size);
            if (merchants.isEmpty()) {
                log.info("데이터 수집 종료.");
                break;
            }
            redisService.saveToRedis(merchants);
            log.info("{} 페이지 데이터가 Redis에 저장되었습니다.", page);
            page++;
        }
    }

    private List<RegionMnyFacltStusDto> getDataFromAPI(int page, int size) {
        String url = buildApiUrl(page, size);
        log.info("요청 URL: {}", url);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(new URI(url), String.class);
            log.info("응답 상태 코드: {}", responseEntity.getStatusCode().value());
            return handleApiResponse(responseEntity);
        } catch (URISyntaxException e) {
            log.error("URI 문법 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("API 호출 중 오류: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<RegionMnyFacltStusDto> handleApiResponse(ResponseEntity<String> responseEntity) {
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return parseResponse(responseEntity.getBody());
        } else {
            log.warn("API 호출 실패, 상태 코드: {}", responseEntity.getStatusCode().value());
            return Collections.emptyList();
        }
    }

    private List<RegionMnyFacltStusDto> parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            log.warn("응답이 비어 있습니다.");
            return Collections.emptyList();
        }

        try {
            Map<String, Object> map = objectMapper.readValue(response, Map.class);

            // "RESULT" 키가 존재하고 "CODE"가 INFO-200이면 데이터 없음으로 처리
            Map<String, Object> result = (Map<String, Object>) map.get("RESULT");
            if (result != null && "INFO-200".equals(result.get("CODE"))) {
                log.warn("API에서 해당하는 데이터가 없습니다: {}", result.get("MESSAGE"));
                return Collections.emptyList();
            }

            List<Map<String, Object>> regionMnyFacltStusList = (List<Map<String, Object>>) map.get(
                    "RegionMnyFacltStus");

            if (regionMnyFacltStusList == null || regionMnyFacltStusList.size() < 2) {
                log.warn("RegionMnyFacltStus 배열에 예상된 데이터가 없습니다.");
                return Collections.emptyList();
            }

            Map<String, Object> rowData = regionMnyFacltStusList.get(1);
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) rowData.get("row");

            if (itemsList == null) {
                log.warn("row 데이터가 응답에 없습니다.");
                return Collections.emptyList();
            }

            return itemsList.stream()
                    .map(this::mapToDto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private RegionMnyFacltStusDto mapToDto(Map<String, Object> itemMap) {
        RegionMnyFacltStusDto dto = new RegionMnyFacltStusDto();
        dto.setBizRegNo((String) itemMap.get("BIZREGNO"));
        dto.setCmpnmNm((String) itemMap.get("CMPNM_NM"));
        dto.setIndutypeNm((String) itemMap.get("INDUTYPE_NM"));
        dto.setRefineLotnoAddr((String) itemMap.get("REFINE_LOTNO_ADDR"));
        dto.setRefineRoadnmAddr((String) itemMap.get("REFINE_ROADNM_ADDR"));
        dto.setRefineZipNo((String) itemMap.get("REFINE_ZIPNO"));
        dto.setRefineWgs84Logt((String) itemMap.get("REFINE_WGS84_LOGT"));
        dto.setRefineWgs84Lat((String) itemMap.get("REFINE_WGS84_LAT"));
        dto.setSigunNm((String) itemMap.get("SIGUN_NM"));
        return dto;
    }

    private String buildApiUrl(int page, int size) {
        String sigunNm = URLEncoder.encode("군포시", StandardCharsets.UTF_8);
        return "https://openapi.gg.go.kr/RegionMnyFacltStus?KEY=" + apiKey + "&pIndex=" + page + "&pSize=" + size
                + "&SIGUN_NM=" + sigunNm + "&Type=json";
    }

}
