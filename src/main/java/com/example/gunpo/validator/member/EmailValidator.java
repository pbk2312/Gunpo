package com.example.gunpo.validator.member;

import com.example.gunpo.constants.EmailErrorMessage;
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
            throw new DuplicateEmailException(EmailErrorMessage.DUPLICATE_EMAIL.getMessage());
        }
    }

    public void validateStoredData(String storedData) {
        if (storedData == null) {
            throw new VerificationCodeExpiredException(EmailErrorMessage.VERIFICATION_CODE_EXPIRED.getMessage());
        }
    }

    public void validateCertification(String storedCertification, String certificationNumber) {
        if (!storedCertification.equals(certificationNumber)) {
            throw new VerificationCodeMismatchException(EmailErrorMessage.VERIFICATION_CODE_MISMATCH.getMessage());
        }
    }

    public void validateVerificationStatus(String status) {
        if (Boolean.parseBoolean(status)) {
            throw new EmailAlreadyVerifiedException(EmailErrorMessage.EMAIL_ALREADY_VERIFIED.getMessage());
        }
    }

}
