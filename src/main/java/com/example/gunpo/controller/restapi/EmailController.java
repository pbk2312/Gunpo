package com.example.gunpo.controller.restapi;

import com.example.gunpo.dto.EmailDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.email.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
        log.info("이메일 인증 메일 전송 요청: 이메일 = {}", emailDto.getEmail());
        String message = emailService.sendCertificationMail(emailDto);
        log.info("이메일 인증 메일 전송 성공: 이메일 = {}", emailDto.getEmail());
        return ResponseEntity.ok(new ResponseDto<>(message, null, true));
    }

    // 이메일 확인
    @PostMapping("/verifyEmail")
    public ResponseEntity<ResponseDto<?>> verifyEmail(@RequestBody EmailDto emailDto) {
        log.info("이메일 인증 요청: 이메일 = {}, 인증번호 = {}", emailDto.getEmail(), emailDto.getCertificationNumber());
        emailService.verifyEmail(emailDto.getEmail(), emailDto.getCertificationNumber());
        log.info("이메일 인증 성공: 이메일 = {}", emailDto.getEmail());
        return ResponseEntity.ok(new ResponseDto<>("인증번호 인증이 완료되었습니다.", null, true));
    }

}
