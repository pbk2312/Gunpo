package com.example.gunpo.constants;

public enum ImageErrorMessage {
    IMAGE_STORAGE_ERROR("이미지 저장 중 오류가 발생했습니다."),
    INVALID_FILE_FORMAT("지원되지 않는 파일 형식입니다."),
    FILE_TOO_LARGE("파일 크기가 너무 큽니다.");

    private final String message;

    ImageErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
