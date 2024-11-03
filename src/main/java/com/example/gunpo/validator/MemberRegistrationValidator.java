package com.example.gunpo.validator;

import com.example.gunpo.constants.MemberErrorMessage;
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
public class MemberRegistrationValidator {

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    public void validateNewMember(MemberDto memberDto) {
        validateEmailAndPasswordNotNull(memberDto); // 이메일 및 비밀번호 null 체크
        checkEmailDuplication(memberDto.getEmail()); // 이메일 중복 확인
        validateEmailCertification(memberDto); // 이메일 인증 여부 확인
    }

    private void validateEmailAndPasswordNotNull(MemberDto memberDto) {
        if (memberDto.getEmail() == null || memberDto.getPassword() == null) {
            throw new IllegalArgumentException(MemberErrorMessage.EMAIL_REQUIRED_MESSAGE.getMessage());
        }
    }

    private void validateEmailCertification(MemberDto memberDto) {
        String value = redisService.getEmailCertificationFromRedis(memberDto.getEmail());
        if (value == null || !Boolean.parseBoolean(value.split(":")[1])) {
            throw new VerificationCodeMismatchException(
                    MemberErrorMessage.EMAIL_VERIFICATION_FAILED_MESSAGE.getMessage());
        }
    }

    private void checkEmailDuplication(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(MemberErrorMessage.EMAIL_IN_USE_MESSAGE.getMessage());
        }
    }

    public Member validateExistingMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(
                        MemberErrorMessage.MEMBER_NOT_FOUND_ID.getMessage() + memberId));
    }

}
