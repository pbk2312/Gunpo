package com.example.gunpo.service.member;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CertificationNumberGenerator {

    private static final int CERTIFICATION_LENGTH = 6;

    public String generate() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CERTIFICATION_LENGTH; i++) {
            sb.append(random.nextInt(10)); // 0부터 9까지의 난수 생성
        }
        return sb.toString();
    }

}
