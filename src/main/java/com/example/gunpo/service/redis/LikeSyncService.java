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

        // Redis에서 모든 게시물 좋아요 수에 대한 키를 가져옵니다.
        Set<String> keys = stringRedisTemplate.keys("board:like:*");

        if (keys == null || keys.isEmpty()) {
            log.info("동기화할 Redis 키가 없습니다.");
            return;
        }

        // Redis 키를 하나씩 처리하면서 게시물 ID를 추출하고, 해당 게시물의 좋아요 수를 DB에 업데이트
        for (String key : keys) {
            try {
                // "board:like:<boardId>" 형태의 키만 처리
                if (key.split(":").length < 3 || !key.startsWith("board:like:")) {
                    continue; // board:like:<boardId> 형태가 아니면 무시
                }

                // 키에서 boardId 추출 (예: board:like:2 => 2)
                String boardIdStr = key.split(":")[2]; // 키에서 3번째 요소가 boardId
                Long boardId = Long.parseLong(boardIdStr); // boardId를 Long 타입으로 변환

                // 해당 boardId의 좋아요 수 가져오기
                String likeCountStr = stringRedisTemplate.opsForValue().get(key);
                if (likeCountStr != null) {
                    // "liked" 문자열인 경우, 해당 키는 좋아요 기록을 나타내므로 건너뜁니다.
                    if ("liked".equals(likeCountStr)) {
                        continue; // "liked" 값은 좋아요를 눌렀다는 표시로, 좋아요 수 동기화에 사용되지 않습니다.
                    }

                    // 숫자로 변환할 수 있을 경우에만 진행
                    int likeCount = Integer.parseInt(likeCountStr);

                    // Board 객체를 DB에서 찾아서 좋아요 수 업데이트
                    Board board = boardRepository.findById(boardId)
                            .orElseThrow(() -> new InvalidPostIdException("유효하지 않은 게시물 ID: " + boardId));

                    // 좋아요 수를 업데이트하여 DB에 저장
                    boardRepository.save(board.toBuilder().likeCount(likeCount).build());
                    log.info("게시글 ID {}의 좋아요 수 업데이트: {}", boardId, likeCount);
                }
            } catch (Exception e) {
                log.error("게시물 좋아요 수 동기화 중 오류 발생. Key: {}", key, e);
            }
        }

        log.info("좋아요 수 동기화 완료");
    }

}
