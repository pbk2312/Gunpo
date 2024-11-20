package com.example.gunpo.controller.restapi;

import com.example.gunpo.domain.Category;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.exception.board.BoardValidationException;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.member.UnauthorizedException;
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
    public ResponseEntity<ResponseDto<String>> boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam List<MultipartFile> images) {
        try {
            BoardDto boardDto = new BoardDto(title, content, Category.valueOf(category));
            boardCreationService.create(boardDto, accessToken, images);
            return ResponseEntity.ok(new ResponseDto<>("게시물 작성이 성공적으로 완료 되었습니다.", null, true));
        } catch (UnauthorizedException e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        } catch (BoardValidationException e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        } catch (Exception e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
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
        try {
            BoardDto boardDto = new BoardDto(id, title, content, Category.valueOf(category));
            boardUpdateService.updatePost(boardDto, newImages, deleteImages, accessToken);
            return ResponseEntity.ok(new ResponseDto<>("성공적으로 게시물 수정 완료", null, true));
        } catch (CannotFindBoardException e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        } catch (Exception e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto<String>> boardDeletePost(
            @PathVariable Long id,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        try {
            boardService.deletePost(id, accessToken);
            return ResponseEntity.ok(new ResponseDto<>("성공적으로 삭제가 완료되었습니다", null, true));
        } catch (UnauthorizedException e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        } catch (Exception e) {
            ResponseDto<String> responseDto = new ResponseDto<>(e.getMessage(), null, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }

}
