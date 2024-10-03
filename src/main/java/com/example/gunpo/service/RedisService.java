package com.example.gunpo.service;

import com.example.gunpo.domain.SmokingArea;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, SmokingArea> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "smoking_area:";

    public void saveToRedis(List<SmokingArea> smokingAreas) {
        smokingAreas.forEach(smokingArea -> {
            String redisKey = REDIS_KEY_PREFIX + smokingArea.getBoothName(); // 고유 키 설정

            // Redis에 이미 존재하는지 확인
            if (!redisTemplate.hasKey(redisKey)) {
                redisTemplate.opsForValue().set(redisKey, smokingArea); // Redis에 저장
                log.info("Redis에 저장된 흡연구역: {}", smokingArea.getBoothName());
            } else {
                log.info("이미 Redis에 존재하는 흡연구역: {}", smokingArea.getBoothName());
            }
        });
    }
}