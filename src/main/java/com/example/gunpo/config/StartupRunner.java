package com.example.gunpo.config;

import com.example.gunpo.service.GyeonggiMerchantService;
import com.example.gunpo.service.SmokingAreaService;
import lombok.RequiredArgsConstructor;
//
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class StartupRunner {
    private final SmokingAreaService smokingAreaService;
    private final GyeonggiMerchantService gyeonggiMerchantService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            // 모든 흡연 구역 데이터를 가져와 Redis에 저장
            smokingAreaService.fetchAllDataAndSaveToRedis();
            gyeonggiMerchantService.fetchAllDataAndSaveToRedis();
            log.info("모든 흡연 구역 데이터가 Redis에 저장되었습니다.");
        };
    }

}
