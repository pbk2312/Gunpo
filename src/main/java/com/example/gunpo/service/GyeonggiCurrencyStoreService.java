package com.example.gunpo.service;

import com.example.gunpo.dto.GyeonggiCurrencyStoreDto;
import com.example.gunpo.mapper.GyeonggiCurrencyStoreMapper;
import com.example.gunpo.service.redis.RedisGyeonggiCurrencyStoreService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class GyeonggiCurrencyStoreService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Value("${gyeonggi.currency.data.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisGyeonggiCurrencyStoreService redisService;
    private final GyeonggiCurrencyStoreMapper mapper;

    public void fetchAllDataAndSaveToRedis() {
        if (redisService.isDataPresent()) {
            log.info("Redis에 데이터가 이미 존재하므로 API 호출을 중단합니다.");
            return;
        }
        collectAndSaveDataToRedis();
    }

    private void collectAndSaveDataToRedis() {
        int page = 1;

        while (true) {
            List<GyeonggiCurrencyStoreDto> merchants = fetchMerchantsFromApi(page);
            if (merchants.isEmpty()) {
                log.info("데이터 수집 종료.");
                break;
            }
            saveMerchantsToRedis(merchants, page);
            page++;
        }
    }

    private List<GyeonggiCurrencyStoreDto> fetchMerchantsFromApi(int page) {
        String url = buildApiUrl(page, DEFAULT_PAGE_SIZE);
        log.info("요청 URL: {}", url);

        ResponseEntity<String> responseEntity = getApiResponse(url);
        if (responseEntity == null) {
            return Collections.emptyList();
        }

        return handleApiResponse(responseEntity);
    }

    private ResponseEntity<String> getApiResponse(String url) {
        try {
            return restTemplate.getForEntity(new URI(url), String.class);
        } catch (URISyntaxException e) {
            log.error("URI 문법 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("API 호출 중 오류: {}", e.getMessage());
        }
        return null; // 오류 시 null 반환
    }

    private List<GyeonggiCurrencyStoreDto> handleApiResponse(ResponseEntity<String> responseEntity) {
        if (!isResponseSuccessful(responseEntity)) {
            return Collections.emptyList();
        }
        return parseResponseBody(responseEntity.getBody());
    }

    private boolean isResponseSuccessful(ResponseEntity<String> responseEntity) {
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.warn("API 호출 실패, 상태 코드: {}", responseEntity.getStatusCode().value());
            return false;
        }
        return true;
    }

    private List<GyeonggiCurrencyStoreDto> parseResponseBody(String responseBody) {
        if (isEmptyResponse(responseBody)) {
            return Collections.emptyList();
        }

        Map<String, Object> responseMap = parseJsonToMap(responseBody);
        if (responseMap == null || isNoDataResponse(responseMap)) {
            return Collections.emptyList();
        }

        return extractItemsFromResponse(responseMap);
    }

    private boolean isEmptyResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            log.warn("응답이 비어 있습니다.");
            return true;
        }
        return false;
    }

    private Map<String, Object> parseJsonToMap(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            return null;
        }
    }

    private boolean isNoDataResponse(Map<String, Object> responseMap) {
        Map<String, Object> result = (Map<String, Object>) responseMap.get("RESULT");
        if (result != null && "INFO-200".equals(result.get("CODE"))) {
            log.warn("API에서 해당하는 데이터가 없습니다: {}", result.get("MESSAGE"));
            return true;
        }
        return false;
    }

    private List<GyeonggiCurrencyStoreDto> extractItemsFromResponse(Map<String, Object> responseMap) {
        List<Map<String, Object>> regionMnyFacltStusList = getRegionMnyFacltStusList(responseMap);

        if (regionMnyFacltStusList == null || regionMnyFacltStusList.size() < 2) {
            log.warn("RegionMnyFacltStus 배열에 예상된 데이터가 없습니다.");
            return Collections.emptyList();
        }

        Map<String, Object> rowData = regionMnyFacltStusList.get(1);
        List<Map<String, Object>> itemsList = getItemsList(rowData);

        if (itemsList == null) {
            log.warn("row 데이터가 응답에 없습니다.");
            return Collections.emptyList();
        }

        return itemsList.stream()
                .map(mapper::mapToDto)
                .toList();
    }

    private List<Map<String, Object>> getRegionMnyFacltStusList(Map<String, Object> responseMap) {
        return (List<Map<String, Object>>) responseMap.get("RegionMnyFacltStus");
    }

    private List<Map<String, Object>> getItemsList(Map<String, Object> rowData) {
        return (List<Map<String, Object>>) rowData.get("row");
    }

    private String buildApiUrl(int page, int size) {
        String sigunNm = URLEncoder.encode("군포시", StandardCharsets.UTF_8);
        return String.format(
                "https://openapi.gg.go.kr/RegionMnyFacltStus?KEY=%s&pIndex=%d&pSize=%d&SIGUN_NM=%s&Type=json",
                apiKey,
                page,
                size,
                sigunNm
        );
    }

    private void saveMerchantsToRedis(List<GyeonggiCurrencyStoreDto> merchants, int page) {
        redisService.saveToRedis(merchants);
        log.info("{} 페이지 데이터가 Redis에 저장되었습니다.", page);
    }

}
