package com.bazarak.exception.image;

public class FileIsEmptyException extends RuntimeException{
    public FileIsEmptyException(String message){
        super(message);
    }
}
