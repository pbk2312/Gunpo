package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.board.CommentRequestDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.board.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    @PostMapping("/{boardId}")
    public ResponseEntity<ResponseDto<String>> addComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {

        commentService.addComment(boardId, accessToken, requestDto.getContent());

        return ResponseEntity.ok(new ResponseDto<>("댓글 작성 성공!", null, true));

    }


    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseDto<String>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.updateComment(commentId, accessToken,requestDto.getContent());
        return ResponseEntity.ok(new ResponseDto<>("댓글 수정 성공!", null, true));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseDto<String>> deleteComment(
            @PathVariable Long commentId,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        commentService.deleteComment(commentId,accessToken);
        return ResponseEntity.ok(new ResponseDto<>("댓글 삭제 성공!", null, true));
    }

}
