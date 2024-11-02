package com.example.gunpo.service.member;

import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final AuthenticationService authenticationService;

    public TokenService(TokenProvider tokenProvider, AuthenticationService authenticationService) {
        this.tokenProvider = tokenProvider;
        this.authenticationService = authenticationService;
    }

    public ResponseEntity<ResponseDto<Map<String, Object>>> validateTokens(
            String accessToken, String refreshToken, HttpServletResponse response) {

        Map<String, Object> data = new HashMap<>();

        // Access Token 검증
        if (validateToken(accessToken)) {
            return buildResponse("Access token is valid", data, HttpStatus.OK, true);
        }

        // Refresh Token 검증 및 Access Token 재발급 처리
        return Optional.ofNullable(refreshToken)
                .filter(this::validateToken)
                .map(token -> handleRefreshToken(token, response, data))
                .orElseGet(() -> buildResponse("Refresh token is invalid", data, HttpStatus.UNAUTHORIZED, false));
    }

    private boolean validateToken(String token) {
        return token != null && tokenProvider.validate(token);
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> handleRefreshToken(
            String refreshToken, HttpServletResponse response, Map<String, Object> data) {

        return Optional.ofNullable(authenticationService.findByRefreshToken(refreshToken))
                .map(member -> issueNewAccessToken(refreshToken, response, data))
                .orElseGet(() -> buildResponse("Refresh token is invalid", data, HttpStatus.UNAUTHORIZED, false));
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> issueNewAccessToken(
            String refreshToken, HttpServletResponse response, Map<String, Object> data) {

        TokenDto newTokenDTO = generateNewAccessToken(refreshToken, response);
        data.put("isLoggedIn", true);
        data.put("accessToken", newTokenDTO.getAccessToken());
        return buildResponse("New access token issued", data, HttpStatus.OK, true);
    }

    private TokenDto generateNewAccessToken(String refreshToken, HttpServletResponse response) {
        Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
        TokenDto newTokenDTO = tokenProvider.generateTokenDto(authentication);
        CookieUtils.addCookie(response, "accessToken", newTokenDTO.getAccessToken(), 3600);
        return newTokenDTO;
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> buildResponse(String message,
                                                                           Map<String, Object> data,
                                                                           HttpStatus status,
                                                                           boolean isLoggedIn) {
        data.put("isLoggedIn", isLoggedIn);
        return ResponseEntity.status(status).body(new ResponseDto<>(message, data));
    }
}
