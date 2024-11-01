package com.example.gunpo.constants;

public enum MemberErrorMessage {
    INVALID_REFRESH_TOKEN("유효하지 않은 refreshToken입니다."),
    INCORRECT_PASSWORD("비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_USER("인증되지 않은 사용자입니다."),
    MEMBER_NOT_FOUND_EMAIL("해당 이메일에 대한 회원이 존재하지 않습니다."),
    MEMBER_NOT_FOUND_ID("해당 ID의 회원을 찾을 수 없습니다. ID: ");

    private final String message;

    MemberErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
