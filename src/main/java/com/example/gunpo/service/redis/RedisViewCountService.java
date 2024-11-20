package com.example.gunpo.service.redis;

import com.example.gunpo.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisViewCountService {
    private final StringRedisTemplate stringRedisTemplate;

    // 특정 게시물의 조회수를 Redis에 저장
    public void saveViewCountToRedis(Long boardId) {
        String redisKey = RedisConstants.VIEW_COUNT_PREFIX + boardId;
        stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "0");
    }

    // 조회수 증가
    public void incrementViewCountIfNotExists(Long boardId, String userId) {
        String viewKey = RedisConstants.VIEW_COUNT_PREFIX + boardId + ":user:" + userId;
        Boolean isAbsent = stringRedisTemplate.opsForValue().setIfAbsent(viewKey, "viewed");
        log.info("조회수 1+");
        if (isAbsent != null && isAbsent) {
            stringRedisTemplate.expire(viewKey, RedisConstants.VIEW_EXPIRE_TIME, TimeUnit.SECONDS);
            incrementBoardViewCount(boardId);
        }
    }

    // 특정 게시물의 조회수를 Redis에서 가져오기
    public int getViewCountFromRedis(Long boardId) {
        String redisKey = RedisConstants.VIEW_COUNT_PREFIX + boardId;
        String viewCountStr = stringRedisTemplate.opsForValue().get(redisKey);

        if (viewCountStr == null) {
            return 0; // 값이 없으면 0을 반환
        }

        try {
            return Integer.parseInt(viewCountStr);
        } catch (NumberFormatException e) {
            return 0; // 숫자로 변환할 수 없는 경우 0을 반환
        }
    }

    // 여러 게시물의 조회수를 Redis에서 일괄 가져오기
    public Map<Long, Integer> getViewCounts(List<Long> boardIds) {
        List<String> redisKeys = boardIds.stream()
                .map(id -> RedisConstants.VIEW_COUNT_PREFIX + id)
                .toList();

        List<String> viewCountStrings = stringRedisTemplate.opsForValue().multiGet(redisKeys);

        // Redis 결과를 매핑하여 반환
        return boardIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            int index = boardIds.indexOf(id);
                            String countStr =
                                    (viewCountStrings != null && index >= 0) ? viewCountStrings.get(index) : "0";
                            try {
                                return Integer.parseInt(countStr);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        }
                ));
    }

    // 게시물 조회수 증가
    private void incrementBoardViewCount(Long boardId) {
        String redisKey = RedisConstants.VIEW_COUNT_PREFIX + boardId;
        stringRedisTemplate.opsForValue().increment(redisKey);
    }

}
