package com.example.gunpo.service.email;

import com.example.gunpo.dto.EmailDto;
import com.example.gunpo.infrastructure.EmailProvider;
import com.example.gunpo.service.member.CertificationNumberGenerator;
import com.example.gunpo.service.redis.RedisEmailCertificationService;
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
    private final RedisEmailCertificationService redisEmailCertificationService;
    private final CertificationNumberGenerator certificationNumberGenerator;
    private final EmailValidator emailValidator;

    @Transactional
    public String sendCertificationMail(EmailDto emailDto) {
        emailValidator.validateDuplicateEmail(emailDto.getEmail());

        String certificationNumber = certificationNumberGenerator.generate();
        emailProvider.sendCertificationMail(emailDto.getEmail(), certificationNumber);
        redisEmailCertificationService.saveEmailCertificationToRedis(emailDto.getEmail(), certificationNumber);

        return "이메일 전송 성공";
    }

    @Transactional
    public void verifyEmail(String email, String certificationNumber) {
        String storedData = redisEmailCertificationService.getEmailCertificationFromRedis(email);

        emailValidator.validateStoredData(storedData);

        String[] parts = storedData.split(":");
        emailValidator.validateCertification(parts[0], certificationNumber);
        emailValidator.validateVerificationStatus(parts[1]);

        redisEmailCertificationService.updateEmailCertificationInRedis(email, parts[0] + ":true");
    }

}
