package com.bazarak.exception.user;

public class PhoneIsAlreadyUsed extends RuntimeException{
    public PhoneIsAlreadyUsed(String message){
        super(message);
    }
}
