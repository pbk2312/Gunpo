package com.example.gunpo.service.redis;

import com.example.gunpo.dto.RegionMnyFacltStusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisGyeonggiMerchantService {

    private final RedisTemplate<String, RegionMnyFacltStusDto> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "GYEONGGI_MERCHANT:";

    public void saveToRedis(List<RegionMnyFacltStusDto> merchants) {
        merchants.forEach(this::saveMerchantToRedis);
    }

    private void saveMerchantToRedis(RegionMnyFacltStusDto merchant) {
        String redisKey = REDIS_KEY_PREFIX + merchant.getBizRegNo();
        redisTemplate.opsForValue().set(redisKey, merchant);
    }

    public List<RegionMnyFacltStusDto> getAllMerchantsFromRedis() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        List<RegionMnyFacltStusDto> merchantList = new ArrayList<>();
        if (keys != null) {
            keys.forEach(key -> {
                RegionMnyFacltStusDto merchant = redisTemplate.opsForValue().get(key);
                if (merchant != null) {
                    merchantList.add(merchant);
                }
            });
        }
        return merchantList;
    }

}
