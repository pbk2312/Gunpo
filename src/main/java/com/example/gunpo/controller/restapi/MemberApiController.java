package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.MemberUpdateDto;
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
            memberManagementService.save(memberDto);
            return ResponseEntity.ok(new ResponseDto<>("회원가입 성공", null, true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (VerificationCodeMismatchException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (EmailDuplicationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("Unexpected error during sign-up: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>("예기치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", null, false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<String>> login(
            @RequestBody LoginDto loginDto, HttpServletResponse response, HttpServletRequest request) {
        try {
            TokenDto tokenDto = authenticationService.login(loginDto);
            CookieUtils.addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600);
            CookieUtils.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000);
            String redirectUrl = getRedirectUrlFromSession(request);

            return ResponseEntity.ok(new ResponseDto<>("로그인 성공", redirectUrl, true));
        } catch (MemberNotFoundException | IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>("로그인 중 예기치 못한 오류가 발생했습니다.", null, false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<String>> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        try {
            authenticationService.logout(accessToken);
            CookieUtils.removeCookie(response, "accessToken");
            CookieUtils.removeCookie(response, "refreshToken");
            return ResponseEntity.ok(new ResponseDto<>("로그아웃 성공", null, true));
        } catch (UnauthorizedException e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        } catch (Exception e) {
            ResponseDto<String> responseDto = new ResponseDto<>("서버 문제", null, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto<String>> updateMember(
            @RequestBody @Valid MemberUpdateDto updateDto) {
        try {
            memberManagementService.update(updateDto);
            return ResponseEntity.ok(new ResponseDto<>("성공적으로 업데이트 성공", null, true));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("Unexpected error during member update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>("회원 정보 업데이트 중 오류가 발생했습니다.", null, false));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto<String>> deleteMember(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        try {
            memberManagementService.delete(accessToken);
            return ResponseEntity.ok(new ResponseDto<>("회원 탈퇴 성공", null, true));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>(e.getMessage(), null, false));
        } catch (Exception e) {
            log.error("Unexpected error during member deletion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>("회원 탈퇴 중 예기치 못한 오류가 발생했습니다.", null, false));
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<ResponseDto<Map<String, Object>>> validateToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            TokenValidationResult validationResult = authenticationService.validateTokens(accessToken, refreshToken);

            Map<String, Object> data = new HashMap<>();
            data.put("isAccessTokenValid", validationResult.isAccessTokenValid());
            data.put("isRefreshTokenValid", validationResult.isRefreshTokenValid());

            if (validationResult.getNewAccessToken() != null) {
                data.put("accessToken", validationResult.getNewAccessToken());
                CookieUtils.addCookie(response, "accessToken", validationResult.getNewAccessToken(), 3600);
            }

            return ResponseEntity.ok(new ResponseDto<>(validationResult.getMessage(), data, true));
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage(), e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("isAccessTokenValid", false);
            errorData.put("isRefreshTokenValid", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>("토큰 검증 중 오류가 발생했습니다.", errorData, false));
        }
    }

    private String getRedirectUrlFromSession(HttpServletRequest request) {
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        request.getSession().removeAttribute("redirectUrl");
        return redirectUrl != null ? redirectUrl : "/";
    }

}
