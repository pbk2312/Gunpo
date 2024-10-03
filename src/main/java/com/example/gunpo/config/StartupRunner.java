package com.example.gunpo.config;

import com.example.gunpo.service.SmokingZoneService;
import lombok.RequiredArgsConstructor;
//
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StartupRunner {
    private final SmokingZoneService smokingZoneService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            // 모든 흡연 구역 데이터를 가져와 Redis에 저장
            smokingZoneService.fetchAllDataAndSaveToRedis();
            System.out.println("모든 흡연 구역 데이터가 Redis에 저장되었습니다.");
        };
    }


}
