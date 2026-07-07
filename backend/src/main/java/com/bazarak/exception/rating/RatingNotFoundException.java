package com.bazarak.exception.rating;

public class RatingNotFoundException extends RuntimeException {
    public RatingNotFoundException(String message) {
        super(message);
    }
}
