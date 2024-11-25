package com.example.gunpo.service.redis;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.Board;
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

        // Redis에서 조회수 관련 키 가져오기
        Set<String> keys = stringRedisTemplate.keys("viewed:board:*");

        if (keys == null || keys.isEmpty()) {
            log.info("동기화할 Redis 키가 없습니다.");
            return;
        }

        // Redis 키를 하나씩 처리하면서 게시물 ID를 추출하고, 해당 게시물의 조회 수를 DB에 업데이트
        for (String key : keys) {
            try {
                // "viewed:board:<boardId>" 형태의 키만 처리 (user 정보는 제외)
                if (!key.startsWith("viewed:board:") || key.split(":").length != 3) {
                    continue; // "viewed:board:<boardId>" 형태만 처리하고, "viewed:board:<boardId>:user:<userId>"는 건너뜀
                }

                // 키에서 boardId 추출 (예: viewed:board:3 => 3)
                String boardIdStr = key.split(":")[2]; // 키에서 3번째 요소가 boardId
                Long boardId = Long.parseLong(boardIdStr); // boardId를 Long 타입으로 변환

                // 해당 boardId의 조회 수 가져오기
                String viewCountStr = stringRedisTemplate.opsForValue().get(key);
                if (viewCountStr != null) {
                    // 숫자로 변환할 수 있을 경우에만 진행
                    int viewCount = Integer.parseInt(viewCountStr);

                    // Board 객체를 DB에서 찾아서 조회 수 업데이트
                    Board board = boardRepository.findById(boardId)
                            .orElseThrow(
                                    () -> new InvalidPostIdException(BoardErrorMessage.INVALID_POST_ID.getMessage()));

                    // 조회 수를 업데이트하여 DB에 저장분
                    boardRepository.save(board.toBuilder().viewCount(viewCount).build());
                    log.info("게시글 ID {}의 조회수 업데이트: {}", boardId, viewCount);
                }
            } catch (Exception e) {
                log.error("게시물 조회수 동기화 중 오류 발생. Key: {}", key, e);
            }
        }

        log.info("조회수 동기화 완료");
    }

}
