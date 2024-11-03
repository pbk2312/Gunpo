package com.example.gunpo.service.redis;

import com.example.gunpo.constants.RedisConstants;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisEmailCertificationService {

    private final StringRedisTemplate stringRedisTemplate;

    public void saveEmailCertificationToRedis(String email, String certificationNumber) {
        String value = certificationNumber + ":false";
        Optional.ofNullable(stringRedisTemplate.opsForValue()).ifPresent(valueOps ->
                valueOps.set(RedisConstants.EMAIL_CERTIFICATION_PREFIX + email, value,
                        RedisConstants.EMAIL_CERTIFICATION_EXPIRE_TIME, TimeUnit.SECONDS)
        );
    }

    public String getEmailCertificationFromRedis(String email) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(RedisConstants.EMAIL_CERTIFICATION_PREFIX + email);
    }

    public void updateEmailCertificationInRedis(String email, String updatedValue) {
        Optional.ofNullable(stringRedisTemplate.opsForValue())
                .ifPresent(valueOps -> valueOps.set(RedisConstants.EMAIL_CERTIFICATION_PREFIX + email, updatedValue));
    }
}
