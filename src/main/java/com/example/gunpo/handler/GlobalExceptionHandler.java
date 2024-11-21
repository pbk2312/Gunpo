package com.example.gunpo.handler;

import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.exception.board.BoardValidationException;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.email.DuplicateEmailException;
import com.example.gunpo.exception.email.EmailAlreadyVerifiedException;
import com.example.gunpo.exception.email.EmailSendFailedException;
import com.example.gunpo.exception.email.VerificationCodeExpiredException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.exception.location.DistanceCalculationException;
import com.example.gunpo.exception.location.InvalidCoordinateException;
import com.example.gunpo.exception.member.EmailDuplicationException;
import com.example.gunpo.exception.member.IncorrectPasswordException;
import com.example.gunpo.exception.member.MemberNotFoundException;
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


    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ResponseDto<String>> handleMemberNotFoundException(MemberNotFoundException e) {
        log.error("MemberNotFoundException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(EmailDuplicationException.class)
    public ResponseEntity<ResponseDto<String>> handleEmailDuplicationException(EmailDuplicationException e) {
        log.error("EmailDuplicationException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ResponseDto<String>> handleIncorrectPasswordException(IncorrectPasswordException e) {
        log.error("IncorrectPasswordException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(VerificationCodeMismatchException.class)
    public ResponseEntity<ResponseDto<String>> handleVerificationCodeMismatchException(
            VerificationCodeMismatchException e) {
        log.error("VerificationCodeMismatchException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
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


    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ResponseDto<String>> handleDuplicateEmailException(DuplicateEmailException e) {
        log.error("DuplicateEmailException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ResponseDto<String>> handleEmailAlreadyVerifiedException(EmailAlreadyVerifiedException e) {
        log.warn("EmailAlreadyVerifiedException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(EmailSendFailedException.class)
    public ResponseEntity<ResponseDto<String>> handleEmailSendFailedException(EmailSendFailedException e) {
        log.error("EmailSendFailedException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ResponseDto<String>> handleVerificationCodeExpiredException(
            VerificationCodeExpiredException e) {
        log.error("VerificationCodeExpiredException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(InvalidCoordinateException.class)
    public ResponseEntity<ResponseDto<String>> handleInvalidCoordinateException(InvalidCoordinateException e) {
        log.error("InvalidCoordinateException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

    @ExceptionHandler(DistanceCalculationException.class)
    public ResponseEntity<ResponseDto<String>> handleDistanceCalculationException(DistanceCalculationException e) {
        log.error("DistanceCalculationException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDto<>(e.getMessage(), null, false));
    }

}
