package com.example.gunpo.handler;

import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.exception.board.BoardValidationException;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.member.UnauthorizedException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseDto<String>> handleUnauthorizedException(UnauthorizedException e) {
        log.error("UnauthorizedException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(BoardValidationException.class)
    public ResponseEntity<ResponseDto<String>> handleBoardValidationException(BoardValidationException e) {
        log.error("BoardValidationException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(CannotFindBoardException.class)
    public ResponseEntity<ResponseDto<String>> handleCannotFindBoardException(CannotFindBoardException e) {
        log.error("CannotFindBoardException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<String>> handleGeneralException(Exception e) {
        log.error("예기치 못한 오류 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDto<>("서버 내부 오류가 발생했습니다.", null, false));
    }

}
