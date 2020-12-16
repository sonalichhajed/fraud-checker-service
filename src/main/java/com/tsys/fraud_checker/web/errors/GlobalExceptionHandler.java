package com.tsys.fraud_checker.web.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

/**
 * The @ControllerAdvice annotation allows us to consolidate our
 * multiple, scattered @ExceptionHandlers from before into a single,
 * global error handling component.
 *
 * The actual mechanism is extremely simple but also very flexible:
 *
 * - It gives us full control over the body of the response as well
 *   as the status code.
 * - It provides mapping of several exceptions to the same method, to
 *   be handled together.
 * - It makes good use of the newer RESTful ResposeEntity response.
 *
 * Keep in mind to match the exceptions declared with @ExceptionHandler
 * to the exception used as the argument of the method.
 *
 * In case there is no match, the compiler will not complain — no
 * reason it should — and Spring will not complain either.
 *
 * In such a case, when the exception is actually thrown at runtime,
 * the exception resolving mechanism will fail with:
 *
 * java.lang.IllegalStateException: No suitable resolver for argument [0]
 * [type=...] HandlerMethod details: ..
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * What we’re doing here is simply reading information about
     * the violations out of the exceptions and translating them
     * into our ValidationErrorResponse data structure.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorsResponse onConstraintValidationException(
            ConstraintViolationException e) {
        var errors = new ValidationErrorsResponse();
        e.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationError(violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .forEach(validationError ->  errors.add(validationError));
        return errors;
    }

    /**
     * What we’re doing here is simply reading information about
     * the violations out of the exceptions and translating them
     * into our ValidationErrorResponse data structure.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorsResponse onMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        var errors = new ValidationErrorsResponse();
        e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .forEach(validationError -> errors.add(validationError));
        return errors;
    }
}