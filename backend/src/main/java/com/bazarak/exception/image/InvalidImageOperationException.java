package com.bazarak.exception.image;

public class InvalidImageOperationException extends RuntimeException{
    public InvalidImageOperationException(String message){
        super(message);
    }
}
