package com.example.gunpo.constants;

public enum EmailErrorMessage {
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다."),
    VERIFICATION_CODE_EXPIRED("인증번호가 만료되었거나 존재하지 않습니다."),
    VERIFICATION_CODE_MISMATCH("인증번호가 일치하지 않습니다."),
    EMAIL_ALREADY_VERIFIED("이미 인증된 이메일입니다."),
    EMAIL_SEND_FAILURE("이메일 발송에 실패했습니다.");

    private final String message;

    EmailErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
