package com.tsys.fraud_checker.web.errors;

public class ValidationError {
    public final String fieldName;

    public final String message;

    public ValidationError(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }
}
