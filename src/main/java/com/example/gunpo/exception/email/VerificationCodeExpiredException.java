package com.example.gunpo.exception.email;

public class VerificationCodeExpiredException extends RuntimeException{

    public VerificationCodeExpiredException(String message) {
        super(message);
    }

}
