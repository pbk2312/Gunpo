package com.example.gunpo.constants;

public enum NewsErrorMessage {

    FETCH_DOCUMENT_ERROR("뉴스 데이터를 가져오는 중 오류가 발생했습니다."),
    PARSE_NEWS_ERROR("뉴스 데이터를 파싱하는 중 오류가 발생했습니다."),
    GENERAL_ERROR("알 수 없는 오류가 발생했습니다.");

    private final String message;

    NewsErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
