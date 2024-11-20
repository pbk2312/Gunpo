package com.example.gunpo.constants;

public class ApiConstants {
    public static class SmokingArea {
        public static final String BASE_API_URL = "https://api.odcloud.kr/api/15122343/v1/uddi:20726a37-6b6a-4b28-8370-9097991eca6a";
        public static final String QUERY_TEMPLATE = "?returnType=json&serviceKey=%s&page=%d&size=%d";

    }

    public static final int DEFAULT_PAGE_SIZE = 100;

    public static class GyeonggiCurrencyStore {
        public static final String API_BASE_URL = "https://openapi.gg.go.kr/RegionMnyFacltStus";
        public static final String API_QUERY_FORMAT = "%s?KEY=%s&pIndex=%d&pSize=%d&SIGUN_NM=%s&Type=json";
        public static final String DEFAULT_SIGUN_NM = "군포시";
    }

    private ApiConstants() {
        // 인스턴스화 방지
    }

}
