package com.example.gunpo.service.redis;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    public void setStringValue(String memberId, String token, Long expirationTime) {
        Optional.ofNullable(stringRedisTemplate.opsForValue()).ifPresent(valueOps ->
                valueOps.set(memberId, token, expirationTime, TimeUnit.MILLISECONDS)
        );
    }

    public void deleteStringValue(String memberId) {
        stringRedisTemplate.delete(memberId);
    }

    public String findMemberIdByRefreshToken(String refreshToken) {
        Set<String> keys = Optional.ofNullable(stringRedisTemplate.keys("*")).orElse(Set.of());
        return findMemberIdByToken(refreshToken, keys);
    }

    private String findMemberIdByToken(String refreshToken, Set<String> keys) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        for (String key : keys) {
            String storedToken = stringValueOperations.get(key);
            if (refreshToken.equals(storedToken)) {
                return key;
            }
        }
        return null;
    }

}
