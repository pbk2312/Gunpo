package com.example.gunpo.controller.restapi;

import com.example.gunpo.domain.board.Category;
import com.example.gunpo.dto.board.BoardDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.board.BoardCreationService;
import com.example.gunpo.service.board.BoardService;
import com.example.gunpo.service.board.BoardUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    public ResponseEntity<ResponseDto<String>> boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam List<MultipartFile> images) {
        log.info("게시물 작성 요청 - 제목: {}, 카테고리: {}, 이미지 개수: {}", title, category, images.size());
        BoardDto boardDto = new BoardDto(title, content, Category.valueOf(category));
        boardCreationService.create(boardDto, accessToken, images);
        log.info("게시물 작성 성공 - 제목: {}", title);
        return ResponseEntity.ok(new ResponseDto<>("게시물 작성이 성공적으로 완료 되었습니다.", null, true));
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto<String>> boardUpdatePost(
            @RequestParam Long id,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam List<MultipartFile> newImages,
            @RequestParam(required = false) List<String> deleteImages) {
        log.info("게시물 수정 요청 - ID: {}, 제목: {}, 카테고리: {}", id, title, category);
        BoardDto boardDto = new BoardDto(id, title, content, Category.valueOf(category));
        boardUpdateService.updatePost(boardDto, newImages, deleteImages, accessToken);
        log.info("게시물 수정 성공 - ID: {}", id);
        return ResponseEntity.ok(new ResponseDto<>("성공적으로 게시물 수정 완료", null, true));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto<String>> boardDeletePost(
            @PathVariable Long id,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        log.info("게시물 삭제 요청 - ID: {}", id);
        boardService.deletePost(id, accessToken);
        log.info("게시물 삭제 성공 - ID: {}", id);
        return ResponseEntity.ok(new ResponseDto<>("성공적으로 삭제가 완료되었습니다", null, true));
    }

}
