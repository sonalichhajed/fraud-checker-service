package com.tsys.fraud_checker.spring.validators;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class NumberOfDigitsValidatorTest {

  private static Validator validator;

  final TestObject invalidObject = new TestObject(23L);

  @BeforeAll
  public static void setUpClass() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void isValid() {
    Set<ConstraintViolation<TestObject>> constraintViolations =
            validator.validate(new TestObject(2345L));
    assertThat(constraintViolations, hasSize(0));
  }

  @Test
  public void isInvalid() {
    Set<ConstraintViolation<TestObject>> constraintViolations =
            validator.validate(invalidObject);
    assertThat(constraintViolations, hasSize(1));
  }

  @Test
  public void invalidObjectFailsWithCustomMessage() throws Exception {
    Set<ConstraintViolation<TestObject>> constraintViolations =
            validator.validate(invalidObject);
    final var constraintViolation = constraintViolations.stream()
            .findFirst()
            .orElseThrow(() -> new Exception("No constraint violation on invalid object has been raised!"));
    assertThat(constraintViolation.getMessage(), is("Must be 4 digits"));
  }

  class TestObject {

    @NumberOfDigits(value = 4, message = "Must be 4 digits")
    public final Long id;

    public TestObject(Long id) {
      this.id = id;
    }
  }
}
