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

    // 게시글 좋아요/취소 토글
    @PostMapping("/{boardId}/like")
    public ResponseEntity<ResponseDto<Object>> toggleLike(@PathVariable Long boardId,
                                                          @CookieValue(value = "accessToken", required = false) String accessToken,
                                                          @RequestParam boolean isLike) {
        int updatedLikeCount = boardLikeService.toggleLike(boardId, accessToken, isLike);
        String message = isLike ? "Liked successfully" : "Unlike successful";
        return ResponseEntity.ok(new ResponseDto<>(message, updatedLikeCount, true));
    }

    // 게시글 좋아요 수 조회
    @GetMapping("/{boardId}/like-count")
    public ResponseEntity<ResponseDto<Integer>> getLikeCount(@PathVariable Long boardId) {
        int likeCount = boardLikeService.getLikeCount(boardId);
        return ResponseEntity.ok(new ResponseDto<>("Fetched like count successfully", likeCount, true));
    }

    // 사용자 좋아요 여부 확인
    @GetMapping("/{boardId}/is-liked")
    public ResponseEntity<ResponseDto<Boolean>> isUserLiked(@PathVariable Long boardId,
                                                            @CookieValue(value = "accessToken", required = false) String accessToken) {
        boolean isLiked = boardLikeService.isUserLiked(boardId, accessToken);
        return ResponseEntity.ok(new ResponseDto<>("Fetched like status successfully", isLiked, true));
    }

}
