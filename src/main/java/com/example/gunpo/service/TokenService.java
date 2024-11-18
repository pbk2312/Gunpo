package com.example.gunpo.service;


import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.infrastructure.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenService {

    private final TokenProvider tokenProvider;

    // 토큰 생성
    public TokenDto generateTokenDto(Authentication authentication) {
        return tokenProvider.generateTokenDto(authentication);
    }

    public Authentication getAuthentication(String accessToken) {
        return tokenProvider.getAuthentication(accessToken);
    }

    public boolean validateToken(String token) {
        return token != null && tokenProvider.validate(token);
    }

    public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
        return tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
    }

}
