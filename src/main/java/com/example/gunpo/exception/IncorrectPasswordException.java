package com.example.gunpo.exception;

public class IncorrectPasswordException extends RuntimeException{

    public IncorrectPasswordException(String message) {
        super(message);
    }

}
