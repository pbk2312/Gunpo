package com.example.gunpo.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        String errorMessage = "고객님의 등급은 접근할수없습니다.";
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect("/error?message=" + encodedMessage);
    }

}
