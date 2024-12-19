package com.example.gunpo.controller.restapi;

import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.board.BoardLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardLikeController {

    private final BoardLikeService boardLikeService;

    private static final String LIKE_SUCCESS = "좋아요 완료";
    private static final String UNLIKE_SUCCESS = "좋아요 취소 완료";
    private static final String FETCH_LIKE_COUNT_SUCCESS = "좋아요 수를 성공적으로 가져왔습니다";
    private static final String FETCH_LIKE_STATUS_SUCCESS = "좋아요 상태를 성공적으로 가져왔습니다";

    // 게시글 좋아요/취소 토글
    @PostMapping("/{boardId}/like")
    public ResponseEntity<ResponseDto<Object>> toggleLike(@PathVariable Long boardId,
                                                          @CookieValue(value = "accessToken", required = false) String accessToken,
                                                          @RequestParam boolean isLike) {
        int updatedLikeCount = boardLikeService.toggleLike(boardId, accessToken, isLike);
        String message = isLike ? LIKE_SUCCESS : UNLIKE_SUCCESS;
        return ResponseEntity.ok(new ResponseDto<>(message, updatedLikeCount, true));
    }

    // 게시글 좋아요 수 조회
    @GetMapping("/{boardId}/like-count")
    public ResponseEntity<ResponseDto<Integer>> getLikeCount(@PathVariable Long boardId) {
        int likeCount = boardLikeService.getLikeCount(boardId);
        return ResponseEntity.ok(new ResponseDto<>(FETCH_LIKE_COUNT_SUCCESS, likeCount, true));
    }

    // 사용자 좋아요 여부 확인
    @GetMapping("/{boardId}/is-liked")
    public ResponseEntity<ResponseDto<Boolean>> isUserLiked(@PathVariable Long boardId,
                                                            @CookieValue(value = "accessToken", required = false) String accessToken) {
        boolean isLiked = boardLikeService.isUserLiked(boardId, accessToken);
        return ResponseEntity.ok(new ResponseDto<>(FETCH_LIKE_STATUS_SUCCESS, isLiked, true));
    }

}
