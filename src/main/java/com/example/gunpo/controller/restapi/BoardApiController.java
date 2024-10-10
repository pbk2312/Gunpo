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
    public ResponseEntity<ResponseDto<?>> boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody BoardDto boardDto) {
        try {
            Long postId = boardService.createPost(boardDto, accessToken);
            ResponseDto<Long> responseDto = new ResponseDto<>("게시글이 성공적으로 작성되었습니다.",postId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto); // 201 Created 응답
        } catch (Exception e) {
            // 예외 처리: 적절한 메시지를 담은 ResponseDto를 반환
            ResponseDto<String> errorResponse = new ResponseDto<>(null, "게시글 작성에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 400 Bad Request 응답
        }
    }

}
