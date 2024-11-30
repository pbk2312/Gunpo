package com.example.gunpo.constants.errorMessage;

public enum ChatErrorMessage {

    MESSAGE_SERIALIZATION_FAILED("메시지를 직렬화하는 데 실패했습니다."),
    MESSAGE_PUBLISH_FAILED("메시지 발행 중 오류가 발생했습니다.");

    private final String message;

    ChatErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
