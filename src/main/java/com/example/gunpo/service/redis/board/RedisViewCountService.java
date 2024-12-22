package com.example.gunpo.service.redis.board;

import com.example.gunpo.constants.RedisConstants;
import com.example.gunpo.repository.BoardRepository;
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
    private final BoardRepository boardRepository;

    private String generateBoardViewKey(Long boardId) {
        return RedisConstants.VIEW_COUNT_PREFIX + boardId;
    }

    private String generateUserViewKey(Long boardId, Long userId) {
        return RedisConstants.VIEW_COUNT_PREFIX + boardId + ":user:" + userId;
    }

    private int parseRedisValue(String value, String redisKey) {
        if (value == null) {
            log.info("Redis에서 키 {}의 값이 없어서 0 반환", redisKey);
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Redis에서 키 {}의 값 파싱 오류: {}", redisKey, value);
            return 0;
        }
    }

    // 특정 게시물의 조회수를 Redis에 저장
    public void saveViewCountToRedis(Long boardId) {
        String redisKey = generateBoardViewKey(boardId);
        stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "0");
        log.info("Redis에 저장된 조회수 키: {}, 값: 0", redisKey);
    }

    // 조회수 증가
    public void incrementViewCountIfNotExists(Long boardId, Long userId) {
        String userViewKey = generateUserViewKey(boardId, userId); // 사용자별 조회 키 생성
        boolean isNewUserView = markUserView(userViewKey); // 중복 조회 여부 확인

        if (isNewUserView) {
            incrementBoardViewCount(boardId); // 중복이 아닌 경우에만 조회수 증가
        }
    }

    private boolean markUserView(String userViewKey) {
        Boolean isAbsent = stringRedisTemplate.opsForValue().setIfAbsent(userViewKey, "viewed");
        if (isAbsent != null && isAbsent) {
            stringRedisTemplate.expire(userViewKey, RedisConstants.VIEW_EXPIRE_TIME, TimeUnit.SECONDS);
            log.info("Redis에 조회수 키가 존재하지 않아서 설정됨: {} -> 값: viewed", userViewKey);
            return true;
        } else {
            log.info("Redis에 조회수 키가 이미 존재: {}", userViewKey);
            return false;
        }
    }

    // 특정 게시물의 조회수를 Redis 또는 DB에서 가져옴
    public int getViewCount(Long boardId) {
        String redisKey = generateBoardViewKey(boardId);

        // Redis에서 조회
        String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisValue != null) {
            log.info("Redis에서 조회수 가져옴: {} -> {}", redisKey, redisValue);
            return parseRedisValue(redisValue, redisKey);
        }

        // Redis에 없으면 DB 조회
        log.info("Redis에 조회수가 없어 DB에서 조회: boardId={}", boardId);
        int dbViewCount = fetchViewCountFromDB(boardId);

        // Redis 갱신
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbViewCount));
        log.info("DB 조회수를 Redis에 저장: {} -> {}", redisKey, dbViewCount);

        return dbViewCount;
    }

    // 여러 게시물의 조회수를 Redis에서 가져오기
    public Map<Long, Integer> getViewCounts(List<Long> boardIds) {
        List<String> redisKeys = boardIds.stream()
                .map(this::generateBoardViewKey)
                .toList();

        List<String> values = stringRedisTemplate.opsForValue().multiGet(redisKeys);

        return mapViewCounts(boardIds, values);
    }

    private Map<Long, Integer> mapViewCounts(List<Long> boardIds, List<String> values) {
        return boardIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            int index = boardIds.indexOf(id);
                            String value = (values != null && index >= 0) ? values.get(index) : null;
                            return parseRedisValue(value, generateBoardViewKey(id));
                        }
                ));
    }

    // 게시물 조회수 증가
    private void incrementBoardViewCount(Long boardId) {
        String redisKey = generateBoardViewKey(boardId);
        stringRedisTemplate.opsForValue().increment(redisKey);
        log.info("Redis에서 게시물 {}의 조회수 증가, 새 값: {}", boardId, getViewCount(boardId));
    }

    private int fetchViewCountFromDB(Long boardId) {
        return boardRepository.findViewCountByBoardId(boardId)
                .orElse(0); // 조회된 값이 없으면 기본값 0 반환
    }

}
