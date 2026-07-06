package com.bazarak.exception.category;

public class CategoryHasSubCategoriesException extends RuntimeException {
    public CategoryHasSubCategoriesException(String message) {
        super(message);
    }
}
