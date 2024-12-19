package com.example.gunpo.jwt;

import com.example.gunpo.exception.location.NeighborhoodVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String LOGIN_ERROR_MESSAGE = "로그인이 필요한 서비스입니다.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String redirectUrl = "/login";

        if (authException.getCause() instanceof NeighborhoodVerificationException) {
            redirectUrl = "/neighborhoodVerification";
        }

        String encodedMessage = URLEncoder.encode(LOGIN_ERROR_MESSAGE, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl + "?errorMessage=" + encodedMessage);
    }

}
