package com.example.gunpo.util;

import com.example.gunpo.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    public static <T> ResponseEntity<ResponseDto<T>> buildSuccessResponse(String message, T data) {
        ResponseDto<T> response = new ResponseDto<>(message, data);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ResponseDto<T>> buildErrorResponse(String message, HttpStatus status) {
        ResponseDto<T> response = new ResponseDto<>(message, null);
        return ResponseEntity.status(status).body(response);
    }

}
