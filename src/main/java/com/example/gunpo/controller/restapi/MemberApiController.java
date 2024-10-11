package com.example.gunpo.controller.restapi;


import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.MemberSaveException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberApiController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseDto<?>> signUp(@Valid @RequestBody MemberDto memberDto) {
        try {
            Long saveMemberId = memberService.save(memberDto);
            log.info("저장된 회원 아이디: {}", saveMemberId);
            return createSuccessResponse(saveMemberId);
        } catch (MemberSaveException e) {
            log.error("회원가입 오류: {}", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (VerificationCodeMismatchException e) {
            log.error("인증 오류: {}", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IncorrectPasswordException e) {
            log.error("비밀번호 불일치: {}", e.getMessage());
            return createErrorResponse("입력한 비밀번호가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("회원가입 처리 중 예외 발생: {}", e.getMessage());
            return createErrorResponse("회원가입 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ResponseDto<?>> createSuccessResponse(Long memberId) {
        ResponseDto<Long> response = new ResponseDto<>("회원가입이 성공적으로 완료되었습니다.", memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ResponseEntity<ResponseDto<?>> createErrorResponse(String message, HttpStatus status) {
        ResponseDto<String> response = new ResponseDto<>(message, null);
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Map<String, String>>> login(
            @RequestBody LoginDto loginDto,
            HttpServletResponse response,
            HttpServletRequest request) {

        Map<String, String> responseMap = new HashMap<>();
        try {
            // 로그인 서비스 호출 및 토큰 처리
            TokenDto tokenDto = memberService.login(loginDto);
            setTokensInCookies(response, tokenDto);

            // 리다이렉트 URL 처리
            String redirectUrl = getRedirectUrlFromSession(request);

            responseMap.put("accessToken", tokenDto.getAccessToken());
            responseMap.put("redirectUrl", redirectUrl);

            return buildResponse(HttpStatus.OK, "로그인 성공", responseMap);

        } catch (MemberNotFoundException | IncorrectPasswordException e) {
            return buildErrorResponse(e, e instanceof MemberNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 쿠키에 토큰 저장 메서드
    private void setTokensInCookies(HttpServletResponse response, TokenDto tokenDto) {
        addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600);
        addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000);
    }

    // 세션에서 리다이렉트 URL 가져오는 메서드
    private String getRedirectUrlFromSession(HttpServletRequest request) {
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        request.getSession().removeAttribute("redirectUrl");
        return redirectUrl != null ? redirectUrl : "/";
    }

    // 응답 객체 생성 메서드
    private ResponseEntity<ResponseDto<Map<String, String>>> buildResponse(HttpStatus status, String message, Map<String, String> data) {
        return ResponseEntity.status(status).body(new ResponseDto<>(message, data));
    }

    // 오류 응답 객체 생성 메서드
    private ResponseEntity<ResponseDto<Map<String, String>>> buildErrorResponse(Exception e, HttpStatus status) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", e.getMessage());
        return buildResponse(status, "로그인 실패", errorMap);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<?>> logout(@CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {

        // accessToken이 없을 경우 예외 처리
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto<>("로그인된 사용자가 없습니다.", null));
        }

        // 회원 정보 가져오기
        Member member = memberService.getUserDetails(accessToken);

        // 로그아웃 처리
        memberService.logout(member);

        // 쿠키 제거
        removeCookie(response, "accessToken");
        removeCookie(response, "refreshToken");

        // 성공 응답 반환
        return ResponseEntity.ok(new ResponseDto<>("로그아웃 성공", null));
    }

    @GetMapping("/validateToken")
    public ResponseEntity<ResponseDto<Map<String, Object>>> validateToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        Map<String, Object> data = new HashMap<>();

        try {
            // Access Token 검증 및 처리
            if (isValidAccessToken(accessToken)) {
                return buildSuccessResponse("Access token is valid", data, true);
            }

            // Refresh Token 검증 및 새로운 Access Token 발급 처리
            if (isValidRefreshToken(refreshToken)) {
                return handleRefreshTokenValidation(refreshToken, response, data);
            }

            // Refresh Token이 유효하지 않은 경우
            return buildFailureResponse("Refresh token is invalid", data);

        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return buildFailureResponse("Error occurred during token validation", data);
        }
    }

    // Access Token 유효성 검증
    private boolean isValidAccessToken(String accessToken) {
        return accessToken != null && tokenProvider.validate(accessToken);
    }

    // Refresh Token 유효성 검증
    private boolean isValidRefreshToken(String refreshToken) {
        return refreshToken != null && tokenProvider.validate(refreshToken);
    }

    // Refresh Token 검증 후 새 Access Token 발급 처리
    private ResponseEntity<ResponseDto<Map<String, Object>>> handleRefreshTokenValidation(
            String refreshToken, HttpServletResponse response, Map<String, Object> data) throws Exception {

        Member member = memberService.findByRefreshToken(refreshToken);
        if (member != null) {
            // 새 Access Token 발급 및 쿠키 저장
            TokenDto newTokenDTO = generateNewAccessToken(refreshToken, response);

            data.put("isLoggedIn", true);
            data.put("accessToken", newTokenDTO.getAccessToken());
            return buildSuccessResponse("New access token issued", data, true);
        }
        return buildFailureResponse("Refresh token is invalid", data);
    }

    // 새 Access Token 발급 메서드
    private TokenDto generateNewAccessToken(String refreshToken, HttpServletResponse response) throws Exception {
        Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
        TokenDto newTokenDTO = tokenProvider.generateTokenDto(authentication);
        addCookie(response, "accessToken", newTokenDTO.getAccessToken(), 3600); // 1시간 유효
        return newTokenDTO;
    }

    // 성공 응답 생성 메서드
    private ResponseEntity<ResponseDto<Map<String, Object>>> buildSuccessResponse(String message, Map<String, Object> data, boolean isLoggedIn) {
        data.put("isLoggedIn", isLoggedIn);
        return ResponseEntity.ok(new ResponseDto<>(message, data));
    }

    // 실패 응답 생성 메서드
    private ResponseEntity<ResponseDto<Map<String, Object>>> buildFailureResponse(String message, Map<String, Object> data) {
        data.put("isLoggedIn", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>(message, data));
    }


    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = createCookie(name, value, maxAge);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = createCookie(name, null, 0);
        response.addCookie(cookie);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // https 환경에서 사용하려면 true로
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

}
