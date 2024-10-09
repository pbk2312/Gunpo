package com.example.gunpo.exception.email;

public class EmailAlreadyVerifiedException extends RuntimeException{

    public EmailAlreadyVerifiedException(String message) {
        super(message);
    }

}
