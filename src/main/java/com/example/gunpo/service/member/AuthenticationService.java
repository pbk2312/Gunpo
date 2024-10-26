package com.example.gunpo.service.member;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.TokenDto;

public interface AuthenticationService {

    /**
     * 회원 로그인 기능을 수행합니다.
     * @param loginDto 로그인할 회원 정보
     * @return 로그인한 회원 정보와 함께 토큰을 반환
     */
    TokenDto login(LoginDto loginDto);

    /**
     * 회원 로그아웃 기능을 수행합니다.
     * @param memberId 로그아웃할 회원의 ID
     */
    void logout(Long memberId);

    /**
     * RefreshToken을 이용해 회원 정보를 찾습니다.
     * @param refreshToken 회원의 refreshToken
     * @return refreshToken으로 조회한 회원 정보
     */
    Member findByRefreshToken(String refreshToken);

    /**
     * AccessToken을 통해 회원 세부 정보를 가져옵니다.
     * @param accessToken 회원의 accessToken
     * @return accessToken을 통해 인증된 회원 정보
     */
    Member getUserDetails(String accessToken);

}
