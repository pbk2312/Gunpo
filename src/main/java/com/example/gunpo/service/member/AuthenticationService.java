package com.example.gunpo.service.member;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthenticationService {

    /**
     * 회원 로그인 기능을 수행합니다.
     *
     * @param loginDto 로그인할 회원 정보
     * @return 로그인한 회원 정보와 함께 토큰을 반환
     */
    TokenDto login(LoginDto loginDto);

    /**
     * 회원 로그아웃 기능을 수행합니다.
     *
     * @param accessToken 로그아웃할 회원의 accessToken
     */
    void logout(String accessToken);

    /**
     * RefreshToken을 이용해 회원 정보를 찾습니다.
     *
     * @param refreshToken 회원의 refreshToken
     * @return refreshToken으로 조회한 회원 정보
     */
    Member findByRefreshToken(String refreshToken);

    /**
     * AccessToken을 통해 회원 세부 정보를 가져옵니다.
     *
     * @param accessToken 회원의 accessToken
     * @return accessToken을 통해 인증된 회원 정보
     */
    Member getUserDetails(String accessToken);

    /**
     * AccessToken 및 RefreshToken을 검증하고 필요한 경우 새로운 AccessToken을 발급합니다.
     *
     * @param accessToken 회원의 accessToken
     * @param refreshToken 회원의 refreshToken
     * @param response HTTP 응답 객체 (쿠키 저장에 사용)
     * @return 토큰 유효성 검증 결과와 로그인 상태를 포함한 응답
     */
    ResponseEntity<ResponseDto<Map<String, Object>>> validateTokens(String accessToken, String refreshToken, HttpServletResponse response);

}
