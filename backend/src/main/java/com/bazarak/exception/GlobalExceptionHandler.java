package com.bazarak.exception;

import com.bazarak.exception.city.*;
import com.bazarak.exception.user.*;
import com.bazarak.exception.auth.*;
import com.bazarak.exception.category.*;
import com.bazarak.exception.rating.*;
import com.bazarak.exception.advertisement.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle database errors (like unique constraint violations)
    /**
     * Check if any error occurred in database, this situation is handles
     * by costume exception-handlers (in user package), but these errors
     * happen if TWO users register at the EXACT SAME time with the same username.
     * @param ex spring class to handle errors
     * @return if duplicate field were passed to database, the database will throw a unique constraint violation.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        // This is a fallback - should rarely happen because you have custom checks
        Map<String, String> error = new HashMap<>();
        error.put("error", "A field with this value already exists or is invalid");
        error.put("details", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // these are our custom error handlers

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
     * Check if email is already used.
     * @param ex spring class to handle errors
     * @return HTTP-status -> FORBIDDEN
     */
    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<?> handleEmailIsUsed(EmailIsAlreadyUsed ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }


    /**
     * Handles cases where a city is not found in the system.
     * Thrown when trying to retrieve, update, or delete a non-existent city.
     *
     * @param ex the CityNotFoundException
     * @return HTTP 404 NOT_FOUND with error message
     */
    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<?> handleCityNotFound(CityNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles cases where a city name already exists in the system.
     * Thrown when trying to create or update a city with a duplicate name.
     *
     * @param ex the CityNameAlreadyExistsException
     * @return HTTP 409 CONFLICT with error message
     */
    @ExceptionHandler(CityNameAlreadyExistsException.class)
    public ResponseEntity<?> handleCityNameAlreadyExists(CityNameAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles cases where a city cannot be deleted because it has associated advertisements.
     * Thrown when trying to delete a city that is referenced by one or more ads.
     *
     * @param ex the CityHasAdvertisementsException
     * @return HTTP 400 BAD_REQUEST with error message
     */
    @ExceptionHandler(CityHasAdvertisementsException.class)
    public ResponseEntity<?> handleCityHasAdvertisements(CityHasAdvertisementsException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ============================================
    // CATEGORY EXCEPTIONS
    // ============================================

    /**
     * Handles cases where a category is not found in the system.
     * This exception is thrown when trying to retrieve, update, or delete
     * a category that does not exist in the database.
     *
     * @param ex the CategoryNotFoundException containing the error message
     * @return HTTP 404 NOT_FOUND with error details
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<?> handleCategoryNotFound(CategoryNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles cases where a category name already exists in the system.
     * This exception is thrown when trying to create or update a category
     * with a name that is already taken by another category.
     *
     * @param ex the CategoryNameAlreadyExistsException containing the error message
     * @return HTTP 409 CONFLICT with error details
     */
    @ExceptionHandler(CategoryNameAlreadyExistsException.class)
    public ResponseEntity<?> handleCategoryNameAlreadyExists(CategoryNameAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles cases where a category cannot be deleted because it has sub-categories.
     * This exception is thrown when trying to delete a parent category that still
     * contains child categories. The user must delete or re-assign sub-categories first.
     *
     * @param ex the CategoryHasSubCategoriesException containing the error message
     * @return HTTP 400 BAD_REQUEST with error details
     */
    @ExceptionHandler(CategoryHasSubCategoriesException.class)
    public ResponseEntity<?> handleCategoryHasSubCategories(CategoryHasSubCategoriesException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles cases where a category cannot be deleted because it has associated advertisements.
     * This exception is thrown when trying to delete a category that is currently being used
     * by one or more advertisements. The user must re-assign those ads to another category first.
     *
     * @param ex the CategoryHasAdvertisementsException containing the error message
     * @return HTTP 400 BAD_REQUEST with error details
     */
    @ExceptionHandler(CategoryHasAdvertisementsException.class)
    public ResponseEntity<?> handleCategoryHasAdvertisements(CategoryHasAdvertisementsException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ============================================
    // RATING EXCEPTIONS
    // ============================================

    /**
     * Handles cases where a rating is not found in the system.
     * This exception is thrown when trying to retrieve, update, or delete
     * a rating that does not exist in the database.
     *
     * @param ex the RatingNotFoundException containing the error message
     * @return HTTP 404 NOT_FOUND with error details
     */
    @ExceptionHandler(RatingNotFoundException.class)
    public ResponseEntity<?> handleRatingNotFound(RatingNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles cases where a user attempts to rate the same seller for the same advertisement more than once.
     * This exception is thrown when a buyer tries to submit a duplicate rating,
     * which violates the unique constraint on (buyer_id, seller_id, advertisement_id).
     *
     * @param ex the DuplicateRatingException containing the error message
     * @return HTTP 409 CONFLICT with error details
     */
    @ExceptionHandler(DuplicateRatingException.class)
    public ResponseEntity<?> handleDuplicateRating(DuplicateRatingException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles cases where an invalid rating value is provided.
     * This exception is thrown when the rating score is not between 1 and 5,
     * or when the user tries to rate themselves.
     *
     * @param ex the InvalidRatingValueException containing the error message
     * @return HTTP 400 BAD_REQUEST with error details
     */
    @ExceptionHandler(InvalidRatingValueException.class)
    public ResponseEntity<?> handleInvalidRatingValue(InvalidRatingValueException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
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

    /**
     * Check if the advertisement exist.
     * @param ex spring class to handle errors
     * @return HTTP-status -> NOT_FOUND
     */
    @ExceptionHandler(AdNotFoundException.class)
    public ResponseEntity<?> handleAdNotFound(AdNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Check if the advertisement is already deleted.
     * @param ex spring class to handle errors
     * @return HTTP-status -> BAD_REQUEST
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<?> handleInvalidOperation(InvalidOperationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Check if the entered price is positive.
     * @param ex spring class to handle errors
     * @return HTTP-status -> BAD_REQUEST
     */
    @ExceptionHandler(InvalidPriceInputException.class)
    public ResponseEntity<?> handleInvalidInput(InvalidPriceInputException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Check if the user attempting to do the operation owns the ad.
     * @param ex spring class to handle errors
     * @return HTTP-status -> FORBIDDEN
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
}