package com.example.gunpo.constants.errorMessage;

import lombok.Getter;

@Getter
public enum TokenErrorMessage {
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    UNSUPPORTED_TOKEN("지원되지 않는 토큰입니다."),
    TOKEN_MALFORMED("잘못된 형식의 토큰입니다."),
    TOKEN_ILLEGAL_ARGUMENT("JWT 토큰이 잘못되었습니다.");

    private final String message;

    TokenErrorMessage(String message) {
        this.message = message;
    }

}
