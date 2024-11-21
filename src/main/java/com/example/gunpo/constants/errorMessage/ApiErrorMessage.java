package com.example.gunpo.constants.errorMessage;

public enum ApiErrorMessage {

    URI_SYNTAX_ERROR("URI 문법 오류: %s"),
    API_CALL_ERROR("API 호출 중 오류: %s"),
    EMPTY_RESPONSE("응답이 비어 있습니다."),
    NO_DATA_RESPONSE("API에서 해당하는 데이터가 없습니다."),
    MISSING_REGION_MNY_FACLT_STUS("RegionMnyFacltStus 배열에 예상된 데이터가 없습니다."),
    MISSING_ROW_DATA("row 데이터가 응답에 없습니다.");

    private final String message;

    ApiErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage(Object... args) {
        return String.format(message, args);
    }

}
