package com.example.gunpo.validator.member;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.exception.member.EmailDuplicationException;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.service.redis.RedisEmailCertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberRegistrationValidator {

    private final MemberRepository memberRepository;
    private final RedisEmailCertificationService emailCertificationService;

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
        String value = emailCertificationService.getEmailCertificationFromRedis(memberDto.getEmail());
        if (value == null || !Boolean.parseBoolean(value.split(":")[1])) {
            throw new VerificationCodeMismatchException(
                    MemberErrorMessage.EMAIL_VERIFICATION_FAILED_MESSAGE.getMessage());
        }
    }

    private void checkEmailDuplication(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new EmailDuplicationException(MemberErrorMessage.EMAIL_IN_USE_MESSAGE.getMessage());
        }
    }

}
