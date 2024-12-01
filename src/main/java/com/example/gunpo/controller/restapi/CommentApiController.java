package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.board.CommentRequestDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.board.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    // 일반 댓글 추가
    @PostMapping("/{boardId}")
    public ResponseEntity<ResponseDto<String>> addComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.addComment(boardId, accessToken, requestDto.getContent());
        return ResponseEntity.ok(new ResponseDto<>("댓글 작성 성공!", null, true));
    }

    // 대댓글 추가
    @PostMapping("/{boardId}/reply/{parentCommentId}")
    public ResponseEntity<ResponseDto<String>> addReplyComment(
            @PathVariable Long boardId,
            @PathVariable Long parentCommentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.addReplyComment(boardId, parentCommentId, accessToken, requestDto.getContent());
        return ResponseEntity.ok(new ResponseDto<>("대댓글 작성 성공!", null, true));
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseDto<String>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.updateComment(commentId, accessToken, requestDto.getContent());
        return ResponseEntity.ok(new ResponseDto<>("댓글 수정 성공!", null, true));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseDto<String>> deleteComment(
            @PathVariable Long commentId,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.deleteComment(commentId, accessToken);
        return ResponseEntity.ok(new ResponseDto<>("댓글 삭제 성공!", null, true));
    }

    // 대댓글 수정
    @PatchMapping("/{parentCommentId}/reply/{replyId}")
    public ResponseEntity<ResponseDto<String>> updateReplyComment(
            @PathVariable Long parentCommentId,
            @PathVariable Long replyId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        log.info("대댓글 수정 요청 받음: parentCommentId = {}, replyId = {}, content = {}, accessToken = {}",
                parentCommentId, replyId, requestDto.getContent(), accessToken);
        commentService.updateReplyComment(parentCommentId, replyId, accessToken, requestDto.getContent());
        return ResponseEntity.ok(new ResponseDto<>("대댓글 수정 성공!", null, true));
    }

    // 대댓글 삭제
    @DeleteMapping("/{parentCommentId}/reply/{replyId}")
    public ResponseEntity<ResponseDto<String>> deleteReplyComment(
            @PathVariable Long parentCommentId,
            @PathVariable Long replyId,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.deleteReplyComment(parentCommentId, replyId, accessToken);
        return ResponseEntity.ok(new ResponseDto<>("대댓글 삭제 성공!", null, true));
    }

}