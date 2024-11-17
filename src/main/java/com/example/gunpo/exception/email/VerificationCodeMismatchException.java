package com.example.gunpo.exception.email;

public class VerificationCodeMismatchException extends RuntimeException{
    public VerificationCodeMismatchException(String message) {
        super(message);
    }

}
