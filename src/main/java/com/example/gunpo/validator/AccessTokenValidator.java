package com.example.gunpo.validator;

public class AccessTokenValidator implements Validator<String>{
    @Override
    public void validate(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("액세스 토큰이 유효하지 않습니다.");
        }
    }
}
