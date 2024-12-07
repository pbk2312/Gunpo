package com.example.gunpo.email;

import com.example.gunpo.dto.member.EmailDto;
import com.example.gunpo.service.member.CertificationNumberGenerator;
import com.example.gunpo.service.redis.RedisEmailCertificationService;
import com.example.gunpo.validator.member.EmailValidator;
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
        validateEmailForSendingCertification(emailDto.getEmail());
        String certificationNumber = generateCertificationNumber();
        sendCertificationEmail(emailDto.getEmail(), certificationNumber);
        saveCertificationToRedis(emailDto.getEmail(), certificationNumber);

        return "이메일 전송 성공";
    }

    private void validateEmailForSendingCertification(String email) {
        emailValidator.validateDuplicateEmail(email);
    }

    private String generateCertificationNumber() {
        return certificationNumberGenerator.generate();
    }

    private void sendCertificationEmail(String email, String certificationNumber) {
        emailProvider.sendCertificationMail(email, certificationNumber);
    }

    private void saveCertificationToRedis(String email, String certificationNumber) {
        redisEmailCertificationService.saveEmailCertificationToRedis(email, certificationNumber);
    }

    @Transactional
    public void verifyEmail(String email, String certificationNumber) {
        String storedData = getStoredCertificationDataFromRedis(email);
        validateStoredCertificationData(storedData, certificationNumber);
        updateCertificationStatusInRedis(email, storedData);
    }

    private String getStoredCertificationDataFromRedis(String email) {
        String storedData = redisEmailCertificationService.getEmailCertificationFromRedis(email);
        emailValidator.validateStoredData(storedData);
        return storedData;
    }

    private void validateStoredCertificationData(String storedData, String certificationNumber) {
        String[] parts = storedData.split(":");
        emailValidator.validateCertification(parts[0], certificationNumber);
        emailValidator.validateVerificationStatus(parts[1]);
    }

    private void updateCertificationStatusInRedis(String email, String storedData) {
        String[] parts = storedData.split(":");
        redisEmailCertificationService.updateEmailCertificationInRedis(email, parts[0] + ":true");
    }

}
