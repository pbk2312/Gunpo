package com.example.gunpo.exception.board;

public class InvalidPageableException extends RuntimeException {

    public InvalidPageableException(String message) {
        super(message);
    }

    public InvalidPageableException(String message, Throwable cause) {
        super(message, cause);
    }

}
