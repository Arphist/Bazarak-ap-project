package com.bazarak.exception;

import com.bazarak.exception.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // these are our costume error handlers

    /**
     * This method is used if username already exist or not.
     * @param ex spring class to handle errors
     * @return HTTP-status -> CONFLICT
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<?> handleUsernameExists(UsernameAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }
    /**
     * This method is used if email already exist or not.
     * @param ex spring class to handle errors
     * @return HTTP-status -> CONFLICT
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }
    /**
     * This method is used if phone number already exist or not.
     * @param ex spring class to handle errors
     * @return HTTP-status -> CONFLICT
     */
    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<?> handlePhoneExists(PhoneAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }
    /**
     * This method is used if user was not found.
     * @param ex spring class to handle errors
     * @return HTTP-status -> NOT_FOUND
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    /**
     * This method is used if password was not valid.
     * @param ex spring class to handle errors
     * @return HTTP-status -> UNAUTHORIZED
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
    /**
     * This method is used if user is blocked.
     * @param ex spring class to handle errors
     * @return HTTP-status -> FORBIDDEN
     */
    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<?> handleUserBlocked(UserBlockedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }
    /**
     *
     * When an unexpected error occurs that we didn't anticipate,
     * catches any RuntimeException that isn't handled by other specific handlers
     * @param ex spring class to handle errors
     * @return HTTP-status -> BAD_REQUEST
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleGeneric(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * This method builds error response
     * @param status status
     * @param message error message
     * @return ResponseEntity
     */
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }
}