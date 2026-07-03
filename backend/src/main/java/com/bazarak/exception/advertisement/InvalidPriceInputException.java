package com.bazarak.exception.advertisement;

public class InvalidPriceInputException extends RuntimeException{
    public InvalidPriceInputException(String message){
        super(message);
    }
}
