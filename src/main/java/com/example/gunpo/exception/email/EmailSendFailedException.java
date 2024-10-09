package com.example.gunpo.exception.email;

public class EmailSendFailedException extends RuntimeException{
    public EmailSendFailedException(String message) {
        super(message);
    }

}
