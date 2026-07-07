package com.bazarak.exception.rating;

public class InvalidRatingValueException extends RuntimeException {
    public InvalidRatingValueException(String message) {
        super(message);
    }
}
