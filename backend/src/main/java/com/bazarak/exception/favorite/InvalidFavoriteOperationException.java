package com.bazarak.exception.favorite;

public class InvalidFavoriteOperationException extends RuntimeException{
    public InvalidFavoriteOperationException(String message){
        super(message);
    }
}
