package com.example.gunpo.service.redis;

import com.example.gunpo.constants.RedisConstants;
import com.example.gunpo.dto.GyeonggiCurrencyStoreDto;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisGyeonggiCurrencyStoreService {

    private final RedisTemplate<String, GyeonggiCurrencyStoreDto> redisTemplate;

    public boolean isDataPresent() {
        Set<String> keys = redisTemplate.keys(RedisConstants.GYEONGGI_MERCHANT_KEY_PREFIX + "*");
        return keys != null && !keys.isEmpty();
    }

    public void saveToRedis(List<GyeonggiCurrencyStoreDto> merchants) {
        merchants.forEach(merchant -> {
            String redisKey = buildRedisKey(merchant);
            redisTemplate.opsForValue().set(redisKey, merchant);
            log.info("Redis에 저장 완료: {}", redisKey);
        });
    }

    public List<GyeonggiCurrencyStoreDto> getAllMerchants() {
        Set<String> keys = redisTemplate.keys(RedisConstants.GYEONGGI_MERCHANT_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("Redis에 저장된 데이터가 없습니다.");
            return Collections.emptyList();
        }

        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(merchant -> merchant != null)
                .toList();
    }

    public List<GyeonggiCurrencyStoreDto> findByCmpnmNm(String cmpnmNm) {
        return getAllMerchants().stream()
                .filter(merchant -> merchant.getCmpnmNm() != null && merchant.getCmpnmNm().contains(cmpnmNm))
                .toList();
    }

    private String buildRedisKey(GyeonggiCurrencyStoreDto merchant) {
        return RedisConstants.GYEONGGI_MERCHANT_KEY_PREFIX + merchant.getCmpnmNm() + ":" + merchant.getBizRegNo();
    }

}
