package com.example.gunpo.validator;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    // 이메일로 회원 찾기 검증 (통합)
    public Member validateMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(MemberErrorMessage.MEMBER_NOT_FOUND_EMAIL.getMessage()));
    }

    // Access Token 유효성 검증
    public void validateAccessToken(String accessToken) {
        if (!tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException(MemberErrorMessage.UNAUTHORIZED_USER.getMessage());
        }
    }

    // 회원 ID로 회원 찾기 검증
    public Member validateExistingMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(MemberErrorMessage.MEMBER_NOT_FOUND_ID.getMessage() + memberId));
    }

}
