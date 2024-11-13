package com.example.gunpo.validator.member;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.infrastructure.TokenProvider;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.service.redis.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RedisTokenService redisTokenService;
    // 이메일로 회원 찾기 검증
    public Member validateMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(MemberErrorMessage.MEMBER_NOT_FOUND_EMAIL.getMessage()));
    }

    // Access Token 유효성 검증 (null 및 빈 문자열 체크 포함)
    public void validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("액세스 토큰이 유효하지 않습니다.");
        }
        if (!tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException(MemberErrorMessage.UNAUTHORIZED_USER.getMessage());
        }
    }

    // 회원 ID로 회원 찾기 검증
    public Member validateExistingMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(
                        MemberErrorMessage.MEMBER_NOT_FOUND_ID.getMessage() + memberId));
    }

    // Refresh Token을 통해 회원 찾기 검증
    public Member validateMemberByRefreshToken(String refreshToken) {
        String memberId = redisTokenService.findMemberIdByRefreshToken(refreshToken);
        if (memberId == null) {
            throw new UnauthorizedException(MemberErrorMessage.INVALID_REFRESH_TOKEN.getMessage());
        }
        return validateExistingMember(Long.parseLong(memberId));
    }

}
