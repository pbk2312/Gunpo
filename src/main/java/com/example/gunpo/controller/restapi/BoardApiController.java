package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardApiController {

    private final BoardService boardService;

    @PostMapping("/new")
    public ResponseEntity<ResponseDto<Object>> boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody BoardDto boardDto) {

        try {
            Long postId = boardService.createPost(boardDto, accessToken);
            return createResponseEntity(HttpStatus.CREATED, "게시글이 성공적으로 작성되었습니다.", postId);
        } catch (Exception e) {
            return createErrorResponseEntity("게시글 작성에 실패했습니다: " + e.getMessage());
        }
    }

    private ResponseEntity<ResponseDto<Object>> createResponseEntity(HttpStatus status, String message, Object data) {
        ResponseDto<Object> responseDto = new ResponseDto<>(message, data);
        return ResponseEntity.status(status).body(responseDto);
    }

    private ResponseEntity<ResponseDto<Object>> createErrorResponseEntity(String errorMessage) {
        ResponseDto<Object> errorResponse = new ResponseDto<>(null, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
