package com.example.gunpo.validator.member;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.infrastructure.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private final TokenProvider tokenProvider;


    // Access Token 유효성 검증 (null 및 빈 문자열 체크 포함)
    public void validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new UnauthorizedException(MemberErrorMessage.UNAUTHORIZED_USER.getMessage());
        }
        if (!tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException(MemberErrorMessage.UNAUTHORIZED_USER.getMessage());
        }
    }

}
