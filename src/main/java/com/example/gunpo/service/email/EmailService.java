package com.example.gunpo.service.email;

import com.example.gunpo.dto.EmailDto;
import com.example.gunpo.infrastructure.EmailProvider;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.service.CertificationNumberGenerator;
import com.example.gunpo.service.RedisService;
import com.example.gunpo.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final EmailProvider emailProvider;
    private final RedisService redisService;
    private final CertificationNumberGenerator certificationNumberGenerator;
    private final EmailValidator emailValidator;

    @Transactional
    public String sendCertificationMail(EmailDto emailDto) {
        emailValidator.validateDuplicateEmail(emailDto.getEmail());

        String certificationNumber = certificationNumberGenerator.generate();
        emailProvider.sendCertificationMail(emailDto.getEmail(), certificationNumber);
        redisService.saveEmailCertificationToRedis(emailDto.getEmail(), certificationNumber);

        return "이메일 전송 성공";
    }

    @Transactional
    public String verifyEmail(String email, String certificationNumber) {
        String storedData = redisService.getEmailCertificationFromRedis(email);

        emailValidator.validateStoredData(storedData);

        String[] parts = storedData.split(":");
        emailValidator.validateCertification(parts[0], certificationNumber);
        emailValidator.validateVerificationStatus(parts[1]);

        redisService.updateEmailCertificationInRedis(email, parts[0] + ":true");
        return "인증번호 인증 성공";
    }

}
