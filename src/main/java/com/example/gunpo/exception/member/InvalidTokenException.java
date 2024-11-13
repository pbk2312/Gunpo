package com.example.gunpo.exception.member;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
