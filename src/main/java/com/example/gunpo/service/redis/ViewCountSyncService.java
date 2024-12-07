package com.example.gunpo.service.redis;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.board.Board;
import com.example.gunpo.exception.board.InvalidPostIdException;
import com.example.gunpo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class ViewCountSyncService {

    private final StringRedisTemplate stringRedisTemplate;
    private final BoardRepository boardRepository;

    @Scheduled(cron = "0 */5 * * * *") //  5분
    public void syncViewCountsToDatabase() {
        log.info("조회수 동기화 시작");

        Set<String> keys = getRedisKeys();
        if (keys.isEmpty()) {
            log.info("동기화할 Redis 키가 없습니다.");
            return;
        }

        for (String key : keys) {
            processKey(key);
        }

        log.info("조회수 동기화 완료");
    }

    private Set<String> getRedisKeys() {
        return stringRedisTemplate.keys("viewed:board:*");
    }

    private void processKey(String key) {
        try {
            if (!isValidKey(key)) {
                return;
            }

            Long boardId = extractBoardIdFromKey(key);
            String viewCountStr = stringRedisTemplate.opsForValue().get(key);

            if (viewCountStr != null) {
                updateViewCountInDB(boardId, viewCountStr);
            }
        } catch (Exception e) {
            log.error("게시물 조회수 동기화 중 오류 발생. Key: {}", key, e);
        }
    }

    private boolean isValidKey(String key) {
        return key.startsWith("viewed:board:") && key.split(":").length == 3;
    }

    private Long extractBoardIdFromKey(String key) {
        String boardIdStr = key.split(":")[2]; // 키에서 3번째 요소가 boardId
        return Long.parseLong(boardIdStr); // boardId를 Long 타입으로 변환
    }

    private void updateViewCountInDB(Long boardId, String viewCountStr) {
        int viewCount = Integer.parseInt(viewCountStr);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new InvalidPostIdException(BoardErrorMessage.INVALID_POST_ID.getMessage()));

        boardRepository.save(board.toBuilder().viewCount(viewCount).build());
        log.info("게시글 ID {}의 조회수 업데이트: {}", boardId, viewCount);
    }

}
