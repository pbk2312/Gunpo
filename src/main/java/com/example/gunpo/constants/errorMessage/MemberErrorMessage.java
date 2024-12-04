package com.example.gunpo.constants.errorMessage;

public enum MemberErrorMessage {
    INVALID_REFRESH_TOKEN("유효하지 않은 refreshToken입니다."),
    INCORRECT_PASSWORD("비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_USER("인증되지 않은 사용자입니다."),
    MEMBER_NOT_FOUND_EMAIL("해당 이메일에 대한 회원이 존재하지 않습니다."),
    MEMBER_NOT_FOUND_ID("해당 ID의 회원을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND_nickName("해당 닉네임의 회원을 찾을 수 없습니다."),
    EMAIL_REQUIRED_MESSAGE("이메일과 비밀번호는 필수입니다."),
    EMAIL_IN_USE_MESSAGE("이미 사용 중인 이메일입니다."),
    EMAIL_VERIFICATION_FAILED_MESSAGE("이메일 인증이 완료되지 않았습니다."),
    MEMBER_NOT_NeighborhoodVerification("동네 인증이 완료되지 않았습니다."),
    DUPLICATE_NICKNAME("이미 닉네임이 존재합니다.");

    private final String message;

    MemberErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
