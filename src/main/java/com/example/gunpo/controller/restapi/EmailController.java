package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.EmailDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.exception.email.EmailSendFailedException;
import com.example.gunpo.service.EmailService;
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
            // 이메일 전송 서비스 호출
            String message = emailService.sendCertificationMail(emailDto);
            ResponseDto<String> response = new ResponseDto<>(message, null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 사용자 정의 예외 처리
            log.error("이메일 전송 오류: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (EmailSendFailedException e) {
            // 이메일 전송 실패 시 처리
            log.error("이메일 전송 실패: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("알 수 없는 오류 발생: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>("이메일 전송 중 알 수 없는 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 이메일 확인
    @PostMapping("/verifyEmail")
    public ResponseEntity<ResponseDto<?>> verifyEmail(@RequestBody EmailDto emailDto) {
        try {
            emailService.verifyEmail(emailDto.getEmail(), emailDto.getCertificationNumber());
            ResponseDto<String> response = new ResponseDto<>("인증번호 인증이 완료 되었습니다.", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>("인증번호 인증 실패", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}
