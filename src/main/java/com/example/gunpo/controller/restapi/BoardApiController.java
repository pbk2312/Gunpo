package com.example.gunpo.controller.restapi;

import com.example.gunpo.domain.Category;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.board.BoardCreationService;
import com.example.gunpo.service.board.BoardService;
import com.example.gunpo.service.board.BoardUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardApiController {

    private final BoardService boardService;
    private final BoardCreationService boardCreationService;
    private final BoardUpdateService boardUpdateService;

    @PostMapping("/new")
    public ResponseEntity<ResponseDto<Object>> boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam List<MultipartFile> images) {
        try {
            BoardDto boardDto = new BoardDto(title, content, Category.valueOf(category));

            // DTO가 제대로 생성되었는지 확인하는 로그 추가
            log.info("BoardDto created: {}", boardDto.toString());

            Long postId = boardCreationService.create(boardDto, accessToken, images);
            return createResponseEntity(HttpStatus.CREATED, "게시글이 성공적으로 작성되었습니다.", postId);
        } catch (Exception e) {
            return createErrorResponseEntity("게시글 작성에 실패했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto<Object>> boardUpdatePost(
            @RequestParam Long id,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam List<MultipartFile> newImages,
            @RequestParam(required = false) List<String> deleteImages) {
        try {
            BoardDto boardDto = new BoardDto(id, title, content, Category.valueOf(category));

            log.info("BoardDto for update: {}", boardDto.toString());

            boardUpdateService.updatePost(boardDto, newImages, deleteImages, accessToken);

            return createResponseEntity(HttpStatus.OK, "게시글이 성공적으로 수정되었습니다.", boardDto.getId());
        } catch (Exception e) {
            log.error("게시글 수정에 실패했습니다: {}", e.getMessage());
            return createErrorResponseEntity("게시글 수정에 실패했습니다: " + e.getMessage());
        }

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto<Object>> boardDeletePost(
            @PathVariable Long id,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        try {
            // 삭제 요청 - 작성자 권한 확인
            log.info("게시물 삭제 요청 - 게시물 ID: {}", id);
            boardService.deletePost(id, accessToken);

            return createResponseEntity(HttpStatus.OK, "게시글이 성공적으로 삭제되었습니다.", id);
        } catch (Exception e) {
            log.error("게시글 삭제에 실패했습니다: {}", e.getMessage());
            return createErrorResponseEntity("게시글 삭제에 실패했습니다: " + e.getMessage());
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
