package com.example.gunpo.service.redis;

import com.example.gunpo.domain.Board;
import com.example.gunpo.exception.board.InvalidPostIdException;
import com.example.gunpo.repository.BoardRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Log4j2
public class LikeSyncService {

    private final StringRedisTemplate stringRedisTemplate;
    private final BoardRepository boardRepository;

    @Scheduled(cron = "0 */5 * * * *") // 5분
    @Transactional
    public void syncLikeCountsToDatabase() {
        log.info("좋아요 수 동기화 시작");

        // Redis에서 좋아요 키 가져오기
        Set<String> keys = fetchLikeKeys();
        if (keys == null || keys.isEmpty()) {
            log.info("동기화할 Redis 키가 없습니다.");
            return;
        }

        // 키별로 처리
        for (String key : keys) {
            processLikeKey(key);
        }

        log.info("좋아요 수 동기화 완료");
    }

    // Redis에서 좋아요 관련 키 가져오기
    private Set<String> fetchLikeKeys() {
        return stringRedisTemplate.keys("board:like:*");
    }

    // 개별 Redis 키 처리
    private void processLikeKey(String key) {
        try {
            if (!isValidLikeKey(key)) {
                return; // 유효한 키가 아니면 무시
            }

            Long boardId = extractBoardIdFromKey(key);
            int likeCount = fetchLikeCountFromRedis(key);
            if (likeCount >= 0) {
                updateBoardLikeCount(boardId, likeCount);
            }
        } catch (Exception e) {
            log.error("좋아요 수 동기화 중 오류 발생. Key: {}", key, e);
        }
    }

    // 키가 유효한 좋아요 키인지 확인
    private boolean isValidLikeKey(String key) {
        String[] parts = key.split(":");
        return parts.length == 3 && key.startsWith("board:like:");
    }

    // Redis 키에서 게시물 ID 추출
    private Long extractBoardIdFromKey(String key) {
        String boardIdStr = key.split(":")[2];
        return Long.parseLong(boardIdStr);
    }

    // Redis에서 좋아요 수 가져오기
    private int fetchLikeCountFromRedis(String key) {
        String likeCountStr = stringRedisTemplate.opsForValue().get(key);
        if (likeCountStr == null || "liked".equals(likeCountStr)) {
            return -1; // 좋아요 기록만 나타내는 키이거나 값이 없을 경우 처리하지 않음
        }
        return Integer.parseInt(likeCountStr);
    }

    // DB에 좋아요 수 업데이트
    private void updateBoardLikeCount(Long boardId, int likeCount) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new InvalidPostIdException("유효하지 않은 게시물 ID: " + boardId));

        boardRepository.save(board.toBuilder().likeCount(likeCount).build());
        log.info("게시글 ID {}의 좋아요 수 업데이트: {}", boardId, likeCount);
    }

}
