package com.example.gunpo.validator;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberValidator {

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    // 예외 메시지를 상수로 선언하여 관리
    private static final String EMAIL_REQUIRED_MESSAGE = "이메일과 비밀번호는 필수입니다.";
    private static final String EMAIL_IN_USE_MESSAGE = "이미 사용 중인 이메일입니다.";
    private static final String MEMBER_NOT_FOUND_MESSAGE = "회원이 존재하지 않습니다. ID: ";
    private static final String EMAIL_VERIFICATION_FAILED_MESSAGE = "이메일 인증이 완료되지 않았습니다.";

    public void validateNewMember(MemberDto memberDto) {
        validateEmailAndPasswordNotNull(memberDto); // 이메일 및 비밀번호 null 체크
        checkEmailDuplication(memberDto.getEmail()); // 이메일 중복 확인
        validateEmailCertification(memberDto); // 이메일 인증 여부 확인
    }

    public Member validateExistingMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND_MESSAGE + memberId));
    }

    private void validateEmailAndPasswordNotNull(MemberDto memberDto) {
        if (memberDto.getEmail() == null || memberDto.getPassword() == null) {
            throw new IllegalArgumentException(EMAIL_REQUIRED_MESSAGE);
        }
    }

    private void checkEmailDuplication(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(EMAIL_IN_USE_MESSAGE);
        }
    }

    private void validateEmailCertification(MemberDto memberDto) {
        String value = redisService.getEmailCertificationFromRedis(memberDto.getEmail());
        if (value == null || !Boolean.parseBoolean(value.split(":")[1])) {
            throw new VerificationCodeMismatchException(EMAIL_VERIFICATION_FAILED_MESSAGE);
        }
    }

}
