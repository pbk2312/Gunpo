package com.example.gunpo.service.board;

import com.example.gunpo.constants.RedisConstants;
import com.example.gunpo.domain.Member;
import com.example.gunpo.service.member.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log4j2
public class BoardLikeService {

    private final StringRedisTemplate redisTemplate;
    private final AuthenticationService authService;

    public int toggleLike(Long boardId, String accessToken, boolean isLike) {
        Member member = authService.getUserDetails(accessToken);
        String userKey = generateUserKey(boardId, member.getId());

        boolean isActionSuccessful = isLike ? addLike(userKey) : removeLike(userKey);

        if (isActionSuccessful) {
            updateLikeCount(boardId, isLike);
            logLikeAction(member.getId(), boardId, isLike);
        }

        return getLikeCount(boardId);
    }

    public int getLikeCount(Long boardId) {
        String redisKey = generateBoardKey(boardId);
        String likeCountStr = redisTemplate.opsForValue().get(redisKey);

        return parseLikeCount(likeCountStr, boardId);
    }

    public Map<Long, Integer> getLikeCounts(List<Long> boardIds) {
        List<String> redisKeys = boardIds.stream()
                .map(this::generateBoardKey)
                .toList();

        List<String> likeCountStrings = redisTemplate.opsForValue().multiGet(redisKeys);

        return boardIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> parseLikeCount(getLikeCountString(likeCountStrings, boardIds.indexOf(id)), id)
                ));
    }

    public boolean isUserLiked(Long boardId, String accessToken) {
        Member member = authService.getUserDetails(accessToken);
        String userKey = generateUserKey(boardId, member.getId());
        return Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
    }

    private String generateUserKey(Long boardId, Long userId) {
        return RedisConstants.LIKE_PREFIX + boardId + ":user:" + userId;
    }

    private String generateBoardKey(Long boardId) {
        return RedisConstants.LIKE_PREFIX + boardId;
    }

    private boolean addLike(String userKey) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(userKey, "liked", RedisConstants.LIKE_EXPIRE_TIME, TimeUnit.SECONDS));
    }

    private boolean removeLike(String userKey) {
        return Boolean.TRUE.equals(redisTemplate.delete(userKey));
    }

    private void updateLikeCount(Long boardId, boolean isIncrement) {
        String redisKey = generateBoardKey(boardId);
        if (isIncrement) {
            redisTemplate.opsForValue().increment(redisKey);
        } else {
            Long updatedCount = redisTemplate.opsForValue().decrement(redisKey);
            if (updatedCount != null && updatedCount < 0) {
                redisTemplate.opsForValue().set(redisKey, "0");
            }
        }
    }

    private void logLikeAction(Long userId, Long boardId, boolean isLike) {
        String action = isLike ? "좋아요" : "좋아요 취소";
        log.info("사용자 {}가 게시물 {}를 {} 했습니다.", userId, boardId, action);
    }

    private int parseLikeCount(String likeCountStr, Long boardId) {
        if (likeCountStr == null) {
            log.info("게시물 {}의 좋아요 수 데이터가 Redis에 없습니다. 초기값 0 반환.", boardId);
            return 0;
        }

        try {
            return Integer.parseInt(likeCountStr);
        } catch (NumberFormatException e) {
            log.error("게시물 {}의 좋아요 수를 파싱하는 데 실패했습니다. 기본값 0 반환.", boardId);
            return 0;
        }
    }

    private String getLikeCountString(List<String> likeCountStrings, int index) {
        return (likeCountStrings != null && index >= 0) ? likeCountStrings.get(index) : "0";
    }

}
