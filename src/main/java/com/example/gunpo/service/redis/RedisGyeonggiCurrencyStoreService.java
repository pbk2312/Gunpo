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
        merchants.forEach(merchant -> {
            String redisKey = buildRedisKey(merchant);
            saveMerchantToRedis(merchant, redisKey);
            log.info("새 가맹점 정보 저장: {}", redisKey);
        });
    }

    private String buildRedisKey(GyeonggiCurrencyStoreDto merchant) {
        // 상호명과 사업자 등록번호를 조합하여 키 생성
        return REDIS_KEY_PREFIX + merchant.getCmpnmNm() + ":" + merchant.getBizRegNo();
    }

    private void saveMerchantToRedis(GyeonggiCurrencyStoreDto merchant, String redisKey) {
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
