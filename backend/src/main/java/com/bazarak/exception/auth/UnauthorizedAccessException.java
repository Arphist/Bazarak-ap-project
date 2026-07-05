package com.bazarak.exception.auth;

public class UnauthorizedAccessException extends RuntimeException{
    public UnauthorizedAccessException (String message){
        super(message);
    }
}
