package com.example.gunpo.controller.restapi;


import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<ResponseDto<?>> signUp(@RequestBody MemberDto memberDto) {
        try {
            Long saveMemberId = memberService.save(memberDto);
            log.info("저장된 회원 아이디: {}", saveMemberId);
            // 성공적인 응답 반환
            ResponseDto<Long> response = new ResponseDto<>("회원가입이 성공적으로 완료되었습니다.", saveMemberId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // 유효성 검사나 이메일 중복 시 에러 응답
            log.error("회원가입 오류: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        } catch (VerificationCodeMismatchException e) {
            // 인증 실패 시 에러 응답
            log.error("인증 오류: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (IncorrectPasswordException e) {
            log.error("비밀번호 불일치: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>("입력한 비밀번호가 일치하지 않습니다.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("회원가입 처리 중 예외 발생: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>("회원가입 처리 중 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Map<String, String>>> login(
            @RequestBody LoginDto loginDto,
            HttpServletResponse response,
            HttpServletRequest request) {
        Map<String, String> responseMap = new HashMap<>(); // 응답 데이터를 담을 맵 생성

        try {
            // 로그인 서비스 호출
            TokenDto tokenDto = memberService.login(loginDto);

            // 쿠키에 토큰 저장
            addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600); // accessToken 유효기간 1시간
            addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000); // refreshToken 유효기간 7일

            // 세션에 저장된 리다이렉트 URL 가져오기
            String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
            request.getSession().removeAttribute("redirectUrl"); // 사용 후 세션에서 제거

            // 응답 맵에 데이터 추가
            responseMap.put("accessToken", tokenDto.getAccessToken());
            responseMap.put("redirectUrl", redirectUrl != null ? redirectUrl : "/"); // redirectUrl 없을 경우 기본 경로 설정

            // 응답 객체 생성 및 반환
            ResponseDto<Map<String, String>> httpResponse = new ResponseDto<>("로그인 성공", responseMap);
            return ResponseEntity.ok(httpResponse);

        } catch (MemberNotFoundException e) {
            // MemberNotFoundException 발생 시, 구체적인 메시지 전달
            responseMap.put("error", e.getMessage());
            ResponseDto<Map<String, String>> errorResponse = new ResponseDto<>("로그인 실패", responseMap);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IncorrectPasswordException e) {
            // IncorrectPasswordException 발생 시, 구체적인 메시지 전달
            responseMap.put("error", e.getMessage());
            ResponseDto<Map<String, String>> errorResponse = new ResponseDto<>("로그인 실패", responseMap);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            // 기타 예외 발생 시, 구체적인 메시지 전달
            responseMap.put("error", e.getMessage());
            ResponseDto<Map<String, String>> errorResponse = new ResponseDto<>("로그인 중 오류 발생", responseMap);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
            // Access Token 검증
            if (accessToken != null && tokenProvider.validate(accessToken)) {
                data.put("isLoggedIn", true);
                return ResponseEntity.ok(new ResponseDto<>("Access token is valid", data));
            }

            // Access Token이 만료된 경우 Refresh Token 확인
            if (refreshToken != null && tokenProvider.validate(refreshToken)) {
                Member member = memberService.findByRefreshToken(refreshToken);
                if (member != null) {
                    // 새 Access Token 발급
                    Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
                    TokenDto newTokenDTO = tokenProvider.generateTokenDto(authentication);

                    // 새 Access Token을 쿠키에 저장
                    addCookie(response, "accessToken", newTokenDTO.getAccessToken(), 3600); // 1시간 유효

                    data.put("isLoggedIn", true);
                    data.put("accessToken", newTokenDTO.getAccessToken());
                    return ResponseEntity.ok(new ResponseDto<>("New access token issued", data));
                }
            }

            // Refresh Token이 유효하지 않은 경우
            data.put("isLoggedIn", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("Refresh token is invalid", data));

        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            data.put("isLoggedIn", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto<>("Error occurred during token validation", data));
        }
    }


    private ResponseEntity<ResponseDto<?>> buildErrorResponse(HttpStatus status, String message) {
        ResponseDto<?> response = new ResponseDto<>(message, null);
        return ResponseEntity.status(status).body(response);
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
