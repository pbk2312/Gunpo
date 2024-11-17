package com.example.gunpo.validator;

import com.example.gunpo.exception.email.*;
import com.example.gunpo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailValidator {

    private final MemberRepository memberRepository;

    public void validateDuplicateEmail(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }
    }

    public void validateStoredData(String storedData) {
        if (storedData == null) {
            throw new VerificationCodeExpiredException("인증번호가 만료되었거나 존재하지 않습니다.");
        }
    }

    public void validateCertification(String storedCertification, String certificationNumber) {
        if (!storedCertification.equals(certificationNumber)) {
            throw new VerificationCodeMismatchException("인증번호가 일치하지 않습니다.");
        }
    }

    public void validateVerificationStatus(String status) {
        if (Boolean.parseBoolean(status)) {
            throw new EmailAlreadyVerifiedException("이미 인증된 이메일입니다.");
        }
    }

}
