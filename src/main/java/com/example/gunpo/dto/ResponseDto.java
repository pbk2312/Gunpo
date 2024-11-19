package com.example.gunpo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto<T> {

    private String message;
    private T data;
    private boolean success;

}
