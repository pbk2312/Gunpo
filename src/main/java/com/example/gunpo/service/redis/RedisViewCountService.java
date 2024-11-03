package com.example.gunpo.service.redis;


import com.example.gunpo.constants.RedisConstants;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisViewCountService {
    private final StringRedisTemplate stringRedisTemplate;

    public void saveViewCountToRedis(Long boardId) {
        String redisKey = RedisConstants.VIEW_COUNT_PREFIX + boardId;
        stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "0");
    }

    public void incrementViewCountIfNotExists(Long boardId, String userId) {
        String viewKey = RedisConstants.VIEW_COUNT_PREFIX + boardId + ":user:" + userId;
        Boolean isAbsent = stringRedisTemplate.opsForValue().setIfAbsent(viewKey, "viewed");
        log.info("조회수 1+");
        if (isAbsent != null && isAbsent) {
            stringRedisTemplate.expire(viewKey, RedisConstants.VIEW_EXPIRE_TIME, TimeUnit.SECONDS);
            incrementBoardViewCount(boardId);
        }
    }

    public int getViewCountFromRedis(Long boardId) {
        String redisKey = RedisConstants.VIEW_COUNT_PREFIX + boardId;
        String viewCountStr = stringRedisTemplate.opsForValue().get(redisKey);
        try {
            return Integer.parseInt(viewCountStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void incrementBoardViewCount(Long boardId) {
        String redisKey = RedisConstants.VIEW_COUNT_PREFIX + boardId;
        stringRedisTemplate.opsForValue().increment(redisKey);
    }

}
