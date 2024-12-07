package com.example.gunpo.service.board;

import com.example.gunpo.domain.Member;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.board.RedisBoardLikeService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Log4j2
public class BoardLikeService {

    private final RedisBoardLikeService redisService;
    private final AuthenticationService authService;
    private final BoardRepository boardRepository;

    @Transactional
    public int toggleLike(Long boardId, String accessToken, boolean isLike) {
        Member member = authService.getUserDetails(accessToken);

        String userKey = redisService.generateUserKey(boardId, member.getId());
        String boardKey = redisService.generateBoardKey(boardId);

        boolean isActionSuccessful = isLike
                ? redisService.addLike(userKey)
                : redisService.removeLike(userKey);

        if (isActionSuccessful) {
            if (isLike) {
                redisService.incrementLikeCount(boardKey);
            } else {
                redisService.decrementLikeCount(boardKey);
            }
            logLikeAction(member.getId(), boardId, isLike);
        }

        return getLikeCount(boardId);
    }

    @Transactional(readOnly = true)
    public int getLikeCount(Long boardId) {
        String redisKey = redisService.generateBoardKey(boardId);

        // Redis에서 좋아요 수 조회
        String likeCountStr = redisService.getLikeCount(redisKey);
        if (likeCountStr != null) {
            log.info("Redis에서 좋아요 수 가져옴: {} -> {}", redisKey, likeCountStr);
            return parseLikeCount(likeCountStr, boardId);
        }

        // Redis에 데이터가 없으면 DB에서 조회
        log.info("Redis에 좋아요 수 없음. DB에서 조회: boardId={}", boardId);
        int dbLikeCount = fetchLikeCountFromDB(boardId);

        // DB에서 조회한 좋아요 수를 Redis에 저장
        redisService.setLikeCount(redisKey, dbLikeCount);
        log.info("DB의 좋아요 수를 Redis에 저장: {} -> {}", redisKey, dbLikeCount);

        return dbLikeCount;
    }

    private int fetchLikeCountFromDB(Long boardId) {
        // DB에서 좋아요 수 조회
        Optional<Integer> likeCount = boardRepository.findLikeCountByBoardId(boardId);

        // 값이 있으면 반환, 없으면 0을 반환
        return likeCount.orElse(0); // 값이 없으면 기본값 0 반환
    }


    public boolean isUserLiked(Long boardId, String accessToken) {
        Member member = authService.getUserDetails(accessToken);
        String userKey = redisService.generateUserKey(boardId, member.getId());
        return redisService.hasKey(userKey);
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

}
