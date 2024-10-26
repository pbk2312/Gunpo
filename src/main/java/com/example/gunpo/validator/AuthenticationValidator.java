package com.example.gunpo.validator;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 이메일로 회원 찾기 검증
    public Member validateMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일에 대한 회원이 존재하지 않습니다."));
    }

    // 비밀번호 검증
    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    public Member validateExistingMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 ID의 회원을 찾을 수 없습니다. ID: " + memberId));
    }

    public Member validateExistingMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 회원을 찾을 수 없습니다. 이메일: " + email));
    }
}
