package com.example.gunpo.service;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.EmailDto;
import com.example.gunpo.infrastructure.EmailProvider;
import com.example.gunpo.exception.email.EmailAlreadyVerifiedException;
import com.example.gunpo.exception.email.EmailSendFailedException;
import com.example.gunpo.exception.email.VerificationCodeExpiredException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test") // 테스트용 설정
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private EmailProvider emailProvider;

    @MockBean
    private MemberRepository memberRepository;

    @Test
    void sendCertificationMail() {
        // Given
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("test@example.com");

        // 이메일이 이미 사용 중이지 않은 상태로 가정
        when(memberRepository.findByEmail(emailDto.getEmail())).thenReturn(Optional.empty());
        when(emailProvider.sendCertificationMail(anyString(), anyString())).thenReturn(true);

        // When
        String result = emailService.sendCertificationMail(emailDto);

        // Then
        assertEquals("이메일 전송 성공", result);
        verify(redisService, times(1)).saveEmailCertificationToRedis(anyString(), anyString());
    }

    @Test
    void sendCertificationMail_AlreadyUsedEmail() {
        // Given
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("used@example.com");

        // 이미 사용 중인 이메일로 가정
        when(memberRepository.findByEmail(emailDto.getEmail())).thenReturn(Optional.of(new Member()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendCertificationMail(emailDto);
        });
    }

    @Test
    void sendCertificationMail_EmailSendFailed() {
        // Given
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("test@example.com");

        when(memberRepository.findByEmail(emailDto.getEmail())).thenReturn(Optional.empty());
        when(emailProvider.sendCertificationMail(anyString(), anyString())).thenReturn(false); // 이메일 전송 실패

        // When & Then
        assertThrows(EmailSendFailedException.class, () -> {
            emailService.sendCertificationMail(emailDto);
        });
    }

    @Test
    void verifyEmail_Success() {
        // Given
        String email = "test@example.com";
        String certificationNumber = "123456";
        String storedValue = certificationNumber + ":false"; // 인증번호와 인증 상태 저장

        when(redisService.getEmailCertificationFromRedis(email)).thenReturn(storedValue);

        // When
        String result = emailService.verifyEmail(email, certificationNumber);

        // Then
        assertEquals("인증번호 인증 성공", result);
        verify(redisService, times(1)).updateEmailCertificationInRedis(email, certificationNumber + ":true");
    }

    @Test
    void verifyEmail_CodeExpired() {
        // Given
        String email = "test@example.com";
        when(redisService.getEmailCertificationFromRedis(email)).thenReturn(null); // Redis에 값 없음

        // When & Then
        assertThrows(VerificationCodeExpiredException.class, () -> {
            emailService.verifyEmail(email, "123456");
        });
    }

    @Test
    void verifyEmail_CodeMismatch() {
        // Given
        String email = "test@example.com";
        String storedValue = "654321:false"; // 다른 인증번호

        when(redisService.getEmailCertificationFromRedis(email)).thenReturn(storedValue);

        // When & Then
        assertThrows(VerificationCodeMismatchException.class, () -> {
            emailService.verifyEmail(email, "123456");
        });
    }

    @Test
    void verifyEmail_AlreadyVerified() {
        // Given
        String email = "test@example.com";
        String storedValue = "123456:true"; // 이미 인증된 상태

        when(redisService.getEmailCertificationFromRedis(email)).thenReturn(storedValue);

        // When & Then
        assertThrows(EmailAlreadyVerifiedException.class, () -> {
            emailService.verifyEmail(email, "123456");
        });
    }
}
