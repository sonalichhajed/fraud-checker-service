package com.tsys.fraud_checker.spring.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.logging.Logger;

/**
 * Checks number of digits in an double, float, long or int or short number.
 */
public class NumberOfDigitsValidator implements ConstraintValidator<NumberOfDigits, Number> {
    private static final Logger LOG = Logger.getLogger(NumberOfDigitsValidator.class.getName());

    private int value;

    public void initialize(NumberOfDigits parameters) {
        value = parameters.value();
        validateParameters();
    }

    private void validateParameters() {
        if (value < 1)
            throw new IllegalArgumentException("value must be more than 0");
    }

    @Override
    public boolean isValid(Number num, ConstraintValidatorContext context) {
        if (num == null)
            return false;

        var number = num.longValue();
        int count = 0;
        while (number != 0) {
            number = number / 10;
            count++;
        }
        LOG.info(String.format("Number of digits: %d", count));
        return count == value;
    }
}
