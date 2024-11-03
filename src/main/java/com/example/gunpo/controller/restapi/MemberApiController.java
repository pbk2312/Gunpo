package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.member.MemberManagementService;
import com.example.gunpo.util.CookieUtils;
import com.example.gunpo.util.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberApiController {

    private final AuthenticationService authenticationService;
    private final MemberManagementService memberManagementService;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseDto<Long>> signUp(@Valid @RequestBody MemberDto memberDto) {
        try {
            Long memberId = memberManagementService.save(memberDto);
            return ResponseBuilder.buildSuccessResponse("회원가입 성공", memberId);
        } catch (Exception e) {
            return ResponseBuilder.buildErrorResponse("회원가입 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Map<String, String>>> login(
            @RequestBody LoginDto loginDto, HttpServletResponse response, HttpServletRequest request) {
        try {
            TokenDto tokenDto = authenticationService.login(loginDto);
            CookieUtils.addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600);
            CookieUtils.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000);

            // 리다이렉트 URL 처리
            String redirectUrl = getRedirectUrlFromSession(request);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("accessToken", tokenDto.getAccessToken());
            responseData.put("redirectUrl", redirectUrl);
            return ResponseBuilder.buildSuccessResponse("로그인 성공", responseData);
        } catch (Exception e) {
            return ResponseBuilder.buildErrorResponse("로그인 실패: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<String>> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        try {
            authenticationService.logout(accessToken);
            CookieUtils.removeCookie(response, "accessToken");
            CookieUtils.removeCookie(response, "refreshToken");
            return ResponseBuilder.buildSuccessResponse("로그아웃 성공", null);
        } catch (Exception e) {
            return ResponseBuilder.buildErrorResponse("로그아웃 실패: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<ResponseDto<Map<String, Object>>> validateToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        try {
            return authenticationService.validateTokens(accessToken, refreshToken, response);
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return buildFailureResponse();
        }
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> buildFailureResponse() {
        Map<String, Object> data = new HashMap<>();
        data.put("isLoggedIn", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto<>("Error occurred during token validation", data));
    }

    private String getRedirectUrlFromSession(HttpServletRequest request) {
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        request.getSession().removeAttribute("redirectUrl");
        return redirectUrl != null ? redirectUrl : "/";
    }
}
