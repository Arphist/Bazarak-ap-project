package com.bazarak.exception.advertisement;

public class AdNotFoundException extends RuntimeException{
    public AdNotFoundException(String message){
        super(message);
    }
}
