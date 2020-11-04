package com.tsys.fraud_checker.spring.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { NumberOfDigitsValidator.class })
public @interface NumberOfDigits {
  String message() default "{javax.validation.constraints.number_of_digits.message}";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };

  int value();
}
