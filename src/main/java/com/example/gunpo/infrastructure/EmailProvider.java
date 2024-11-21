package com.example.gunpo.infrastructure;

import com.example.gunpo.constants.errorMessage.EmailErrorMessage;
import com.example.gunpo.exception.email.EmailSendFailedException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProvider {

    private final JavaMailSender javaMailSender;

    private static final String SUBJECT = "[Gunpo]";

    public void sendCertificationMail(String email, String certificationNumber) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            String htmlContent = getCertificationMessage(certificationNumber);

            messageHelper.setTo(email);
            messageHelper.setSubject(SUBJECT);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(message);

        } catch (Exception e) {
            throw new EmailSendFailedException(EmailErrorMessage.EMAIL_SEND_FAILURE.getMessage());
        }
    }

    private String getCertificationMessage(String certificationNumber) {
        return String.format(
                "<h1 style='text-align: center;'>[Gunpo] 인증메일</h1>" +
                        "<h3 style='text-align: center;'>인증코드 : <strong style='font-size:32px;letter-spacing:8px;'>%s</strong></h3>",
                certificationNumber
        );
    }

}
