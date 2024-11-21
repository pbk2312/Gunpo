package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.MemberUpdateDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.token.TokenValidationResult;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.member.MemberManagementService;
import com.example.gunpo.util.CookieUtils;
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
    public ResponseEntity<ResponseDto<String>> signUp(@Valid @RequestBody MemberDto memberDto) {
        log.info("회원가입 요청: 이메일 = {}, 닉네임 = {}", memberDto.getEmail(), memberDto.getNickname());
        memberManagementService.save(memberDto);
        log.info("회원가입 성공: 이메일 = {}", memberDto.getEmail());
        return ResponseEntity.ok(new ResponseDto<>("회원가입 성공", null, true));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<String>> login(
            @RequestBody LoginDto loginDto, HttpServletResponse response, HttpServletRequest request) {
        log.info("로그인 요청: 이메일 = {}", loginDto.getEmail());
        TokenDto tokenDto = authenticationService.login(loginDto);
        CookieUtils.addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600);
        CookieUtils.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000);
        String redirectUrl = getRedirectUrlFromSession(request);
        log.info("로그인 성공: 이메일 = {}, 리디렉션 URL = {}", loginDto.getEmail(), redirectUrl);
        return ResponseEntity.ok(new ResponseDto<>("로그인 성공", redirectUrl, true));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<String>> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        log.info("로그아웃 요청: 액세스 토큰 = {}", accessToken);
        authenticationService.logout(accessToken);
        CookieUtils.removeCookie(response, "accessToken");
        CookieUtils.removeCookie(response, "refreshToken");
        log.info("로그아웃 성공: 액세스 토큰 = {}", accessToken);
        return ResponseEntity.ok(new ResponseDto<>("로그아웃 성공", null, true));
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto<String>> updateMember(@RequestBody @Valid MemberUpdateDto updateDto) {
        memberManagementService.update(updateDto);
        return ResponseEntity.ok(new ResponseDto<>("성공적으로 업데이트 성공", null, true));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto<String>> deleteMember(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        memberManagementService.delete(accessToken);
        return ResponseEntity.ok(new ResponseDto<>("회원 탈퇴 성공", null, true));
    }


    @GetMapping("/validateToken")
    public ResponseEntity<ResponseDto<Map<String, Object>>> validateToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        log.info("토큰 검증 요청: 액세스 토큰 = {}, 리프레시 토큰 = {}", accessToken, refreshToken);
        TokenValidationResult validationResult = authenticationService.validateTokens(accessToken, refreshToken);
        Map<String, Object> data = new HashMap<>();
        data.put("isAccessTokenValid", validationResult.isAccessTokenValid());
        data.put("isRefreshTokenValid", validationResult.isRefreshTokenValid());

        if (validationResult.getNewAccessToken() != null) {
            data.put("accessToken", validationResult.getNewAccessToken());
            CookieUtils.addCookie(response, "accessToken", validationResult.getNewAccessToken(), 3600);
        }

        log.info("토큰 검증 성공: 액세스 토큰 유효성 = {}, 리프레시 토큰 유효성 = {}", validationResult.isAccessTokenValid(),
                validationResult.isRefreshTokenValid());
        return ResponseEntity.ok(new ResponseDto<>(validationResult.getMessage(), data, true));
    }

    private String getRedirectUrlFromSession(HttpServletRequest request) {
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        request.getSession().removeAttribute("redirectUrl");
        return redirectUrl != null ? redirectUrl : "/";
    }

}
