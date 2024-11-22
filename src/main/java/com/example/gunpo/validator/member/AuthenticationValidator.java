package com.example.gunpo.validator.member;

import com.example.gunpo.constants.errorMessage.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.location.NeighborhoodVerificationException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.infrastructure.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private final TokenProvider tokenProvider;


    public void validateAccessToken(String accessToken) {
        validateEmpty(accessToken);
        validateToken(accessToken);
    }

    public void validateNeighborhoodVerification(Member member){
        if (!member.isNeighborhoodVerification()){
            throw new NeighborhoodVerificationException(MemberErrorMessage.MEMBER_NOT_NeighborhoodVerification.getMessage());
        }
    }


    private void validateToken(String accessToken) {
        if (!tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException(MemberErrorMessage.UNAUTHORIZED_USER.getMessage());
        }
    }

    private static void validateEmpty(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new UnauthorizedException(MemberErrorMessage.UNAUTHORIZED_USER.getMessage());
        }
    }

}
