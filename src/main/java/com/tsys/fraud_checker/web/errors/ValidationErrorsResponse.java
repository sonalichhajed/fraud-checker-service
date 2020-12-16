package com.tsys.fraud_checker.web.errors;

import java.util.ArrayList;
import java.util.List;

/**
 * Returning Structured Validation Error Responses
 * ===============================================
 * When a validation fails, we want to return a meaningful error
 * message to the client. In order to enable the client to display
 * a helpful error message, we should return a data structure that
 * contains an error message for each validation that failed.
 *
 * First define ValidationErrorResponse and it contains a list of
 * ValidationError objects
 *
 * Then, we create a global ControllerAdvice that handles all
 * ConstraintViolationExceptions that bubble up to the controller
 * level. In order to catch validation errors for request bodies
 * as well, we will also handle MethodArgumentNotValidExceptions:
 * @see GlobalExceptionHandler
 */
public class ValidationErrorsResponse {
    public final List<ValidationError> validationErrors = new ArrayList<>();

    public void add(ValidationError validationError) {
        validationErrors.add(validationError);
    }
}
