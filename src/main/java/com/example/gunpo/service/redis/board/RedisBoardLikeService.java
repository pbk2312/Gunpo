package com.example.gunpo.service.redis.board;

import com.example.gunpo.constants.RedisConstants;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisBoardLikeService {

    private final StringRedisTemplate redisTemplate;

    public String generateUserKey(Long boardId, Long userId) {
        return RedisConstants.LIKE_PREFIX + boardId + ":user:" + userId;
    }

    public String generateBoardKey(Long boardId) {
        return RedisConstants.LIKE_PREFIX + boardId;
    }

    public boolean addLike(String userKey) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(userKey, "liked", RedisConstants.LIKE_EXPIRE_TIME, TimeUnit.SECONDS));
    }

    public boolean removeLike(String userKey) {
        return Boolean.TRUE.equals(redisTemplate.delete(userKey));
    }

    public void incrementLikeCount(String boardKey) {
        redisTemplate.opsForValue().increment(boardKey);
    }

    public void decrementLikeCount(String boardKey) {
        Long updatedCount = redisTemplate.opsForValue().decrement(boardKey);
        if (updatedCount != null && updatedCount < 0) {
            redisTemplate.opsForValue().set(boardKey, "0");
        }
    }

    public String getLikeCount(String boardKey) {
        return redisTemplate.opsForValue().get(boardKey);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Redis에 좋아요 수 저장
    public void setLikeCount(String redisKey, int likeCount) {
        redisTemplate.opsForValue().set(redisKey, String.valueOf(likeCount));
    }

}
