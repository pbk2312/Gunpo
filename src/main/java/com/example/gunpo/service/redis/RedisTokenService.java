package com.example.gunpo.service.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    public void setStringValue(String memberId, String token, Long expirationTime) {
        stringRedisTemplate.opsForValue().set(memberId, token, expirationTime, TimeUnit.MILLISECONDS);
    }

    public void deleteStringValue(String memberId) {
        stringRedisTemplate.delete(memberId);
    }

    // refreshToken 가져오기
    public String getStringValue(String memberId) {
        return stringRedisTemplate.opsForValue().get(memberId);
    }

}
