package com.bazarak.exception.city;

public class CityNameAlreadyExistsException extends RuntimeException {
    public CityNameAlreadyExistsException(String message) {
        super(message);
    }
}
