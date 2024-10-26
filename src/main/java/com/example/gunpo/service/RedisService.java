package com.example.gunpo.service;

import com.example.gunpo.constants.RedisConstants;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, SmokingArea> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // 흡연 구역 Redis에 저장
    public void saveToRedis(List<SmokingArea> smokingAreas) {
        smokingAreas.forEach(this::saveSmokingAreaToRedis);
    }

    // RefreshToken Redis에 저장
    public void setStringValue(String memberId, String token, Long expirationTime) {
        Optional.ofNullable(stringRedisTemplate.opsForValue()).ifPresent(valueOps ->
                valueOps.set(memberId, token, expirationTime, TimeUnit.MILLISECONDS)
        );
    }

    // RefreshToken Redis에서 삭제
    public void deleteStringValue(String memberId) {
        stringRedisTemplate.delete(memberId);
        log.info("Redis에서 키 삭제 완료: {}", memberId);
    }

    // refreshToken으로 memberId 찾기
    public String findMemberIdByRefreshToken(String refreshToken) {
        Set<String> keys = Optional.ofNullable(stringRedisTemplate.keys("*")).orElse(Set.of());
        return findMemberIdByToken(refreshToken, keys);
    }

    // 이메일 인증번호를 Redis에 저장
    public void saveEmailCertificationToRedis(String email, String certificationNumber) {
        String value = certificationNumber + ":false"; // 초기 상태는 false
        Optional.ofNullable(stringRedisTemplate.opsForValue()).ifPresent(valueOps ->
                valueOps.set(RedisConstants.EMAIL_CERTIFICATION_PREFIX + email, value, RedisConstants.EMAIL_CERTIFICATION_EXPIRE_TIME, TimeUnit.SECONDS)
        );
        log.info("이메일 인증번호가 Redis에 저장되었습니다. email: {}", email);
    }

    // 이메일 인증번호를 Redis에서 가져오기
    public String getEmailCertificationFromRedis(String email) {
        String key = createEmailCertificationKey(email);
        return retrieveCertificationNumber(key, email);
    }

    // 인증번호 인증 상태 변경
    public void updateEmailCertificationInRedis(String email, String updatedValue) {
        String key = createEmailCertificationKey(email);
        updateCertificationValue(key, updatedValue);
    }

    // Redis에 조회수 저장
    public void saveViewCountToRedis(Long boardId) {
        log.info("boardId : {} ", boardId);
        String redisKey = createRedisKey(boardId);
        saveViewCountIfAbsent(boardId, redisKey);
    }

    private String createRedisKey(Long boardId) {
        return String.format("%s:%d", RedisConstants.VIEW_COUNT_PREFIX, boardId);
    }

    // 사용자별로 조회 기록을 남기고 조회수 증가
    public boolean incrementViewCountIfNotExists(Long boardId, String userId) {
        String viewKey = RedisConstants.VIEW_COUNT_PREFIX + boardId + ":user:" + userId;
        Boolean isAbsent = Optional.ofNullable(stringRedisTemplate.opsForValue().setIfAbsent(viewKey, "viewed")).orElse(false);

        if (isAbsent) {
            stringRedisTemplate.expire(viewKey, RedisConstants.VIEW_EXPIRE_TIME, TimeUnit.SECONDS);
            incrementBoardViewCount(boardId);
            return true;
        }
        return false;
    }

    private void incrementBoardViewCount(Long boardId) {
        String redisKey = createRedisKey(boardId);
        Optional.ofNullable(stringRedisTemplate.opsForValue()).ifPresent(valueOps -> valueOps.increment(redisKey));
        log.info("게시물 ID {}의 조회수가 증가했습니다.", boardId);
    }

    private void saveViewCountIfAbsent(Long boardId, String redisKey) {
        Boolean isAbsent = Optional.ofNullable(stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "0")).orElse(false);
        if (isAbsent) {
            log.info("게시물 ID {}의 조회수를 Redis에 0으로 저장했습니다.", boardId);
        } else {
            log.info("게시물 ID {}의 조회수가 이미 Redis에 존재합니다.", boardId);
        }
    }

    public int getViewCountFromRedis(Long boardId) {
        String redisKey = createRedisKey(boardId);
        String viewCountStr = Optional.ofNullable(stringRedisTemplate.opsForValue().get(redisKey)).orElse("0");

        try {
            return Integer.parseInt(viewCountStr);
        } catch (NumberFormatException e) {
            log.error("잘못된 조회수 형식입니다. boardId: {}", boardId, e);
            return 0;
        }
    }

    private void saveSmokingAreaToRedis(SmokingArea smokingArea) {
        String redisKey = getRedisKey(smokingArea);
        Boolean exists = Optional.ofNullable(redisTemplate.hasKey(redisKey)).orElse(false);

        if (!exists) {
            redisTemplate.opsForValue().set(redisKey, smokingArea);
            log.info("Redis에 저장된 흡연구역: {}", smokingArea.getBoothName());
        } else {
            log.info("이미 Redis에 존재하는 흡연구역: {}", smokingArea.getBoothName());
        }
    }

    public List<SmokingAreaDto> getAllSmokingZonesFromRedis() {
        Set<String> keys = Optional.ofNullable(redisTemplate.keys(RedisConstants.REDIS_KEY_PREFIX + "*")).orElse(Set.of());
        return getSmokingAreaDtos(keys);
    }

    private String getRedisKey(SmokingArea smokingArea) {
        return RedisConstants.REDIS_KEY_PREFIX + smokingArea.getBoothName();
    }

    private String findMemberIdByToken(String refreshToken, Set<String> keys) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();

        for (String key : keys) {
            String storedToken = stringValueOperations.get(key);
            if (refreshToken.equals(storedToken)) {
                log.info("Redis에서 refreshToken으로 memberId 찾기 완료: {}", key);
                return key;
            }
        }
        return null;
    }

    private String createEmailCertificationKey(String email) {
        return RedisConstants.EMAIL_CERTIFICATION_PREFIX + email;
    }

    private String retrieveCertificationNumber(String key, String email) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String certificationNumber = valueOperations.get(key);

        if (certificationNumber != null) {
            log.info("Redis에서 인증번호를 찾았습니다. email: {}, 인증번호: {}", email, certificationNumber);
            return certificationNumber;
        } else {
            log.warn("Redis에서 인증번호를 찾을 수 없습니다. email: {}", email);
            return null;
        }
    }

    private void updateCertificationValue(String key, String updatedValue) {
        Optional.ofNullable(stringRedisTemplate.opsForValue()).ifPresent(valueOps -> valueOps.set(key, updatedValue));
        log.info("이메일 인증 상태가 Redis에서 업데이트되었습니다. email: {}", key);
    }

    private List<SmokingAreaDto> getSmokingAreaDtos(Set<String> keys) {
        List<SmokingAreaDto> smokingZoneList = new ArrayList<>();

        keys.forEach(key -> {
            SmokingAreaDto dto = getSmokingAreaDtoFromRedis(key);
            if (dto != null) {
                smokingZoneList.add(dto);
            }
        });
        return smokingZoneList;
    }

    private SmokingAreaDto getSmokingAreaDtoFromRedis(String key) {
        SmokingArea smokingArea = redisTemplate.opsForValue().get(key);
        return smokingArea != null ? SmokingZoneMapper.INSTANCE.toDto(smokingArea) : null;
    }

}
