package com.example.gunpo.constants;

public class ApiConstants {
    public static final String BASE_API_URL = "https://api.odcloud.kr/api/15122343/v1/uddi:20726a37-6b6a-4b28-8370-9097991eca6a";
    public static final String API_QUERY_TEMPLATE = "?returnType=json&serviceKey=%s&page=%d&size=%d";
    public static final int DEFAULT_PAGE_SIZE = 100;

    private ApiConstants() {
        // 인스턴스화 방지
    }

}
