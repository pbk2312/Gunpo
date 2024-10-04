package com.example.gunpo.service;

import com.example.gunpo.domain.SmokingArea;
import com.example.gunpo.dto.SmokingAreaDto;
import com.example.gunpo.mapper.SmokingZoneMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    // Redis에 저장된 흡역 구연 정보 가져오기
    public List<SmokingAreaDto> getAllSmokingZonesFromRedis() {
        // Redis에 저장된 모든 흡연 구역 키 가져오기 (REDIS_KEY_PREFIX로 시작하는 키)
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");

        List<SmokingAreaDto> smokingZoneList = new ArrayList<>();

        if (keys != null) {
            keys.forEach(key -> {
                // 각 키에 저장된 SmokingArea 데이터 가져오기
                SmokingArea smokingArea = (SmokingArea) redisTemplate.opsForValue().get(key);
                if (smokingArea != null) {
                    // SmokingArea를 SmokingZoneAreaDto로 변환
                    SmokingAreaDto dto = SmokingZoneMapper.INSTANCE.toDto(smokingArea);
                    smokingZoneList.add(dto); // 리스트에 추가
                }
            });
        }

        return smokingZoneList; // 모든 흡연 구역 리스트 반환
    }
}