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
    private static final long VIEW_EXPIRE_TIME = 24 * 60 * 60L; // 24시간 (초 단위)

    // 흡연 구역 Redis에 저장
    public void saveToRedis(List<SmokingArea> smokingAreas) {
        smokingAreas.forEach(this::saveSmokingAreaToRedis);
    }

    // RefreshToken Redis 에 저장
    public void setStringValue(String memberId, String token, Long expirationTime) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        stringValueOperations.set(memberId, token, expirationTime, TimeUnit.MILLISECONDS);
    }

    // RefreshToken Redis에서 삭제
    public void deleteStringValue(String memberId) {
        stringRedisTemplate.delete(memberId);
        log.info("Redis에서 키 삭제 완료: {}", memberId);
    }

    // refreshToken으로 memberId 찾기
    public String findMemberIdByRefreshToken(String refreshToken) {
        Set<String> keys = stringRedisTemplate.keys("*");
        return findMemberIdByToken(refreshToken, keys);
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
        String key = createEmailCertificationKey(email); // 이메일을 기반으로 키 생성
        return retrieveCertificationNumber(key, email);
    }

    // 인증번호 인증 상태 변경
    public void updateEmailCertificationInRedis(String email, String updatedValue) {
        String key = createEmailCertificationKey(email); // 이메일을 기반으로 키 생성
        updateCertificationValue(key, updatedValue);
    }

    // Redis에 조회수 저장
    public void saveViewCountToRedis(Long boardId) {
        log.info("boardId : {} ", boardId);
        String redisKey = createRedisKey(boardId); // Redis 키 생성
        saveViewCountIfAbsent(boardId, redisKey);
    }

    private String createRedisKey(Long boardId) {
        return String.format("boardId:%d", boardId); // Redis 키 생성 로직을 별도의 메서드로 분리
    }

    // 사용자별로 조회 기록을 남기고 조회수 증가
    public boolean incrementViewCountIfNotExists(Long boardId, String userId) {
        String viewKey = "viewed:board:" + boardId + ":user:" + userId;
        // 사용자별 조회 기록이 존재하지 않으면 조회수 증가
        if (stringRedisTemplate.opsForValue().setIfAbsent(viewKey, "viewed")) {
            stringRedisTemplate.expire(viewKey, VIEW_EXPIRE_TIME, TimeUnit.SECONDS); // 24시간 TTL 설정
            incrementBoardViewCount(boardId); // 조회수 증가
            return true;
        }
        return false; // 이미 조회한 기록이 있으면 조회수 증가하지 않음
    }
    // 게시물 조회수 증가

    private void incrementBoardViewCount(Long boardId) {
        String redisKey = createRedisKey(boardId);
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.increment(redisKey); // 조회수 1 증가
        log.info("게시물 ID {}의 조회수가 증가했습니다.", boardId);
    }

    private void saveViewCountIfAbsent(Long boardId, String redisKey) {
        // 조회수의 초기값을 0으로 설정
        if (stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "0")) {
            log.info("게시물 ID {}의 조회수를 Redis에 0으로 저장했습니다.", boardId);
        } else {
            log.info("게시물 ID {}의 조회수가 이미 Redis에 존재합니다.", boardId);
        }
    }

    // Redis에서 조회수를 가져오는 메서드

    public int getViewCountFromRedis(Long boardId) {
        String redisKey = createRedisKey(boardId);
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String viewCountStr = valueOperations.get(redisKey);

        // 조회수가 존재하면 int로 변환, 없으면 0 반환
        if (viewCountStr != null) {
            try {
                return Integer.parseInt(viewCountStr);
            } catch (NumberFormatException e) {
                log.error("잘못된 조회수 형식입니다. boardId: {}", boardId, e);
            }
        }
        return 0; // 조회수가 없을 경우 0 반환
    }

    private void saveSmokingAreaToRedis(SmokingArea smokingArea) {
        String redisKey = getRedisKey(smokingArea);
        if (!isSmokingAreaInRedis(redisKey)) {
            redisTemplate.opsForValue().set(redisKey, smokingArea);
            logInfoForSavedArea(smokingArea);
        } else {
            logInfoForExistingArea(smokingArea);
        }
    }

    // 흡연 구역들 key 로 가져오기
    public List<SmokingAreaDto> getAllSmokingZonesFromRedis() {
        Set<String> keys = getSmokingZoneKeys();
        return getSmokingAreaDtos(keys);
    }

    private String getRedisKey(SmokingArea smokingArea) {
        return REDIS_KEY_PREFIX + smokingArea.getBoothName();
    }

    private boolean isSmokingAreaInRedis(String redisKey) {
        return redisTemplate.hasKey(redisKey);
    }

    private void logInfoForSavedArea(SmokingArea smokingArea) {
        log.info("Redis에 저장된 흡연구역: {}", smokingArea.getBoothName());
    }

    private void logInfoForExistingArea(SmokingArea smokingArea) {
        log.info("이미 Redis에 존재하는 흡연구역: {}", smokingArea.getBoothName());
    }

    private Set<String> getSmokingZoneKeys() {
        // Redis에 저장된 모든 흡연 구역 키 가져오기 (REDIS_KEY_PREFIX로 시작하는 키)
        return redisTemplate.keys(REDIS_KEY_PREFIX + "*");
    }

    private SmokingAreaDto getSmokingAreaDtoFromRedis(String key) {
        // 각 키에 저장된 SmokingArea 데이터 가져오기
        SmokingArea smokingArea = (SmokingArea) redisTemplate.opsForValue().get(key);
        return (smokingArea != null) ? SmokingZoneMapper.INSTANCE.toDto(smokingArea) : null;
    }

    private String findMemberIdByToken(String refreshToken, Set<String> keys) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();

        if (keys != null) {
            for (String key : keys) {
                String storedToken = stringValueOperations.get(key);
                if (refreshToken.equals(storedToken)) {
                    log.info("Redis에서 refreshToken으로 memberId 찾기 완료: {}", key);
                    return key; // memberId 반환
                }
            }
        }
        return null; // 일치하는 memberId가 없는 경우
    }

    private String createEmailCertificationKey(String email) {
        return "emailCertification:" + email; // 이메일 기반의 키 생성
    }

    private String retrieveCertificationNumber(String key, String email) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String certificationNumber = valueOperations.get(key); // Redis에서 인증번호 가져오기

        if (certificationNumber != null) {
            log.info("Redis에서 인증번호를 찾았습니다. email: {}, 인증번호: {}", email, certificationNumber);
            return certificationNumber;
        } else {
            log.warn("Redis에서 인증번호를 찾을 수 없습니다. email: {}", email);
            return null;
        }
    }

    private void updateCertificationValue(String key, String updatedValue) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(key, updatedValue);
        log.info("이메일 인증 상태가 Redis에서 업데이트되었습니다. email: {}", key);
    }

    private List<SmokingAreaDto> getSmokingAreaDtos(Set<String> keys) {
        List<SmokingAreaDto> smokingZoneList = new ArrayList<>();

        if (keys != null) {
            keys.forEach(key -> {
                SmokingAreaDto dto = getSmokingAreaDtoFromRedis(key);
                if (dto != null) {
                    smokingZoneList.add(dto); // 리스트에 추가
                }
            });
        }
        return smokingZoneList;
    }
}