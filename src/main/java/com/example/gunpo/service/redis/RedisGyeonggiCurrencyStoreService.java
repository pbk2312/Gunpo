package com.example.gunpo.service.redis;

import com.example.gunpo.dto.GyeonggiCurrencyStoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisGyeonggiCurrencyStoreService {

    private final RedisTemplate<String, GyeonggiCurrencyStoreDto> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "GYEONGGI_MERCHANT:";

    public void saveToRedis(List<GyeonggiCurrencyStoreDto> merchants) {
        // Redis에 데이터가 하나라도 있는지 확인
        Set<String> existingKeys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (existingKeys != null && !existingKeys.isEmpty()) {
            log.info("Redis에 이미 데이터가 존재하므로 저장을 중단합니다.");
            return;
        }

        // Redis에 데이터가 없으면 저장 진행
        merchants.forEach(merchant -> {
            String redisKey = REDIS_KEY_PREFIX + merchant.getBizRegNo();
            saveMerchantToRedis(merchant);
            log.info("새 가맹점 정보 저장: {}", redisKey);
        });
    }

    private void saveMerchantToRedis(GyeonggiCurrencyStoreDto merchant) {
        String redisKey = REDIS_KEY_PREFIX + merchant.getBizRegNo();
        redisTemplate.opsForValue().set(redisKey, merchant);
    }

    public List<GyeonggiCurrencyStoreDto> getAllMerchantsFromRedis() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        List<GyeonggiCurrencyStoreDto> merchantList = new ArrayList<>();
        if (keys != null) {
            keys.forEach(key -> {
                GyeonggiCurrencyStoreDto merchant = redisTemplate.opsForValue().get(key);
                if (merchant != null) {
                    merchantList.add(merchant);
                }
            });
        }
        return merchantList;
    }

}
