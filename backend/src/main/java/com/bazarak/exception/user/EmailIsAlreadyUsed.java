package com.bazarak.exception.user;

public class EmailIsAlreadyUsed extends RuntimeException{
    public EmailIsAlreadyUsed(String message){
        super(message);
    }
}
