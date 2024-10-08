package com.example.gunpo.service;

import com.example.gunpo.domain.SmokingArea;
import com.example.gunpo.dto.SmokingAreaDto;
import com.example.gunpo.mapper.SmokingZoneMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, SmokingArea> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
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

    public void setStringValue(String memberId, String token, Long expirationTime) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        stringValueOperations.set(memberId, token, expirationTime, TimeUnit.MILLISECONDS);
    }

    public void deleteStringValue(String memberId) {
        stringRedisTemplate.delete(memberId);
        log.info("Redis에서 키 삭제 완료: {}", memberId);
    }

    // refreshToken으로 memberId 찾기
    public String findMemberIdByRefreshToken(String refreshToken) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        // 모든 키를 순회하면서 refreshToken과 일치하는 값을 찾는다
        for (String key : stringRedisTemplate.keys("*")) {
            String storedToken = stringValueOperations.get(key);
            if (refreshToken.equals(storedToken)) {
                log.info("Redis에서 refreshToken으로 memberId 찾기 완료: {}", key);
                return key; // memberId 반환
            }
        }
        return null;
    }

    // 이메일 인증번호를 Redis에 저장하는 메서드
    public void saveEmailCertificationToRedis(String email, String certificationNumber) {
        String value = certificationNumber + ":false"; // 초기 상태는 false
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set("emailCertification:" + email, value, 60 * 60, TimeUnit.SECONDS); // 1시간 유효
        log.info("이메일 인증번호가 Redis에 저장되었습니다. email: {}", email);
    }

    // 이메일 인증번호를 Redis에서 가져오는 메서드
    public String getEmailCertificationFromRedis(String email) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String key = "emailCertification:" + email; // 이메일을 기반으로 키 생성
        String certificationNumber = valueOperations.get(key); // Redis에서 인증번호 가져오기

        if (certificationNumber != null) {
            log.info("Redis에서 인증번호를 찾았습니다. email: {}, 인증번호: {}", email, certificationNumber);
            return certificationNumber;
        } else {
            log.warn("Redis에서 인증번호를 찾을 수 없습니다. email: {}", email);
            return null;
        }
    }

    public void updateEmailCertificationInRedis(String email, String updatedValue) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set("emailCertification:" + email, updatedValue);
        log.info("이메일 인증 상태가 Redis에서 업데이트되었습니다. email: {}", email);
    }
}