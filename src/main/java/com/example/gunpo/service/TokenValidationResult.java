package com.example.gunpo.service;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TokenValidationResult {
    private final boolean isAccessTokenValid;
    private final boolean isRefreshTokenValid;
    private final String newAccessToken;
    private final String message;

    @Builder
    public TokenValidationResult(boolean isAccessTokenValid, boolean isRefreshTokenValid,
                                 String newAccessToken, String message) {
        this.isAccessTokenValid = isAccessTokenValid;
        this.isRefreshTokenValid = isRefreshTokenValid;
        this.newAccessToken = newAccessToken;
        this.message = message;
    }

}
