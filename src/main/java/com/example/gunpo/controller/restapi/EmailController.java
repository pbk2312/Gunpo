package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.EmailDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.exception.email.DuplicateEmailException;
import com.example.gunpo.exception.email.EmailAlreadyVerifiedException;
import com.example.gunpo.exception.email.EmailSendFailedException;
import com.example.gunpo.exception.email.VerificationCodeExpiredException;
import com.example.gunpo.service.email.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api")
public class EmailController {

    private final EmailService emailService;

    // 인증 메일 보내기
    @PostMapping("/sendCertificationMail")
    public ResponseEntity<ResponseDto<?>> sendCertificationMail(@Valid @RequestBody EmailDto emailDto) {
        try {
            String message = emailService.sendCertificationMail(emailDto);
            log.info("이메일 인증 메일 전송 성공: 이메일 = {}", emailDto.getEmail());
            ResponseDto<String> response = new ResponseDto<>(message, null, true);
            return ResponseEntity.ok(response);
        } catch (DuplicateEmailException e) {
            log.error("이메일 전송 오류 - 중복 이메일: 이메일 = {}, 오류 메시지 = {}", emailDto.getEmail(), e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (EmailSendFailedException e) {
            log.error("이메일 전송 실패: 이메일 = {}, 오류 메시지 = {}", emailDto.getEmail(), e.getMessage(), e);
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: 이메일 = {}, 오류 메시지 = {}", emailDto.getEmail(), e.getMessage(), e);
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 이메일 확인
    @PostMapping("/verifyEmail")
    public ResponseEntity<ResponseDto<?>> verifyEmail(@RequestBody EmailDto emailDto) {
        try {
            log.info("이메일 인증 요청: 이메일 = {}, 인증번호 = {}", emailDto.getEmail(), emailDto.getCertificationNumber());
            emailService.verifyEmail(emailDto.getEmail(), emailDto.getCertificationNumber());
            log.info("이메일 인증 성공: 이메일 = {}", emailDto.getEmail());
            ResponseDto<String> response = new ResponseDto<>("인증번호 인증이 완료 되었습니다.", null, true);
            return ResponseEntity.ok(response);
        } catch (VerificationCodeExpiredException e) {
            log.error("이메일 인증 실패 - 인증번호 만료: 이메일 = {}, 오류 메시지 = {}", emailDto.getEmail(), e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null, true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (EmailAlreadyVerifiedException e) {
            log.warn("이메일 인증 실패 - 이미 인증된 이메일: 이메일 = {}, 오류 메시지 = {}", emailDto.getEmail(), e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null, true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("이메일 인증 중 예기치 않은 오류 발생: 이메일 = {}, 오류 메시지 = {}", emailDto.getEmail(), e.getMessage(), e);
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null, true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
