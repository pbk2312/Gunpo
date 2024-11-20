package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.exception.member.EmailDuplicationException;
import com.example.gunpo.exception.member.IncorrectPasswordException;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.TokenValidationResult;
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
        try {
            log.info("회원가입 요청: 이메일 = {}, 닉네임 = {}", memberDto.getEmail(), memberDto.getNickname());
            memberManagementService.save(memberDto);
            log.info("회원가입 성공: 이메일 = {}", memberDto.getEmail());
            return ResponseEntity.ok(new ResponseDto<>("회원가입 성공", null, true));
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (VerificationCodeMismatchException e) {
            log.warn("회원가입 실패 - 인증 코드 불일치: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (EmailDuplicationException e) {
            log.warn("회원가입 실패 - 이메일 중복: 이메일 = {}, 오류 메시지 = {}", memberDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("회원가입 중 알 수 없는 오류 발생: 이메일 = {}, 오류 메시지 = {}", memberDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<String>> login(
            @RequestBody LoginDto loginDto, HttpServletResponse response, HttpServletRequest request) {
        try {
            log.info("로그인 요청: 이메일 = {}", loginDto.getEmail());
            TokenDto tokenDto = authenticationService.login(loginDto);
            CookieUtils.addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600);
            CookieUtils.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000);
            String redirectUrl = getRedirectUrlFromSession(request);
            log.info("로그인 성공: 이메일 = {}, 리디렉션 URL = {}", loginDto.getEmail(), redirectUrl);
            return ResponseEntity.ok(new ResponseDto<>("로그인 성공", redirectUrl, true));
        } catch (MemberNotFoundException | IncorrectPasswordException e) {
            log.error("로그인 실패 - 사용자 정보 불일치: 이메일 = {}, 오류 메시지 = {}", loginDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("로그인 중 알 수 없는 오류 발생: 이메일 = {}, 오류 메시지 = {}", loginDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<String>> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        try {
            log.info("로그아웃 요청: 액세스 토큰 = {}", accessToken);
            authenticationService.logout(accessToken);
            CookieUtils.removeCookie(response, "accessToken");
            CookieUtils.removeCookie(response, "refreshToken");
            log.info("로그아웃 성공: 액세스 토큰 = {}", accessToken);
            return ResponseEntity.ok(new ResponseDto<>("로그아웃 성공", null, true));
        } catch (UnauthorizedException e) {
            log.error("로그아웃 실패 - 인증되지 않은 사용자: 오류 메시지 = {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("로그아웃 중 알 수 없는 오류 발생: 오류 메시지 = {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<ResponseDto<Map<String, Object>>> validateToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
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
        } catch (Exception e) {
            log.error("토큰 검증 중 알 수 없는 오류 발생: 오류 메시지 = {}", e.getMessage(), e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("isAccessTokenValid", false);
            errorData.put("isRefreshTokenValid", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(e.getMessage(), errorData, false));
        }
    }

    private String getRedirectUrlFromSession(HttpServletRequest request) {
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        request.getSession().removeAttribute("redirectUrl");
        return redirectUrl != null ? redirectUrl : "/";
    }

}
