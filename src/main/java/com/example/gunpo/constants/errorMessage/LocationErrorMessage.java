package com.example.gunpo.constants.errorMessage;


public enum LocationErrorMessage {
    INVALID_LATITUDE("위도(latitude)는 -90 ~ 90 사이의 값이어야 합니다."),
    INVALID_LONGITUDE("경도(longitude)는 -180 ~ 180 사이의 값이어야 합니다."),
    DISTANCE_CALCULATION_ERROR("거리 계산 중 오류가 발생했습니다.");

    private final String message;

    LocationErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
