package com.tsys.fraud_checker.services;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.CreditCardBuilder;
import com.tsys.fraud_checker.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest
// Here’s a validation test at the service level
public class VerificationServiceValidationTest {

  @MockBean
  private Random random;

  @Autowired
  private VerificationService service;

  private final Money chargedAmount = new Money(Currency.getInstance("INR"), 1235.45d);
  private final CreditCard validCard = CreditCardBuilder.make()
          .withHolder("Jumping Jack")
          .withIssuingBank("Bank of Test")
          .withValidNumber()
          .withValidCVV()
          .withFutureExpiryDate()
          .build();

  @Test
  public void isValid() throws InterruptedException {
    given(random.nextInt(anyInt())).willReturn(0);
    final var fraudStatus = service.verifyTransactionAuthenticity(validCard, chargedAmount);
    assertThat(fraudStatus).isNotNull();
  }

  @Test
  public void shoutsWhenCreditCardIsNotPresent() {
    assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(null, chargedAmount);
    });
  }

  @Test
  public void shoutsWhenAmountIsNotPresent() {
    assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(null, null);
    });
  }

  @Test
  public void shoutsWhenCreditCardNumberIsAbsent() {
    CreditCard cardWithoutNumber = CreditCardBuilder.make()
            .withHolder("Card Holder")
            .withIssuingBank("Bank")
            .withFutureExpiryDate()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithoutNumber, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.number: Card number is required");
  }

  @Test
  public void shoutsWhenCreditCardNumberIsEmpty() {
    CreditCard cardWithoutNumber = CreditCardBuilder.make()
            .withHolder("Card Holder")
            .withIssuingBank("Bank")
            .withNumber("")
            .withFutureExpiryDate()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithoutNumber, chargedAmount);
    });
    assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.creditCard.number: Card number is required");
    assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.creditCard.number: Invalid Credit Card Number");
    assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.creditCard.number: Failed Luhn check!");
    assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.creditCard.number: length must be between 16 and 19");
  }

  @Test
  public void shoutsWhenCreditCardHolderIsAbsent() {
    CreditCard cardWithoutHolder = CreditCardBuilder.make()
            .withValidNumber()
            .withIssuingBank("Bank")
            .withFutureExpiryDate()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithoutHolder, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.holderName: is required");
  }

  @Test
  public void shoutsWhenCreditCardHolderIsEmpty() {
    CreditCard cardWithEmptyHolder = CreditCardBuilder.make()
            .withHolder("")
            .withValidNumber()
            .withIssuingBank("Bank")
            .withFutureExpiryDate()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithEmptyHolder, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.holderName: is required");
  }

  @Test
  public void shoutsWhenCreditCardIssuingBankIsAbsent() {
    CreditCard cardWithoutBank = CreditCardBuilder.make()
            .withHolder("Card Without Bank")
            .withValidNumber()
            .withFutureExpiryDate()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithoutBank, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.issuingBank: name is required");
  }

  @Test
  public void shoutsWhenCreditCardIssuingBankIsEmpty() {
    CreditCard cardEmptyBank = CreditCardBuilder.make()
            .withHolder("Card Without Bank")
            .withIssuingBank("")
            .withValidNumber()
            .withFutureExpiryDate()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardEmptyBank, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.issuingBank: name is required");
  }

  @Test
  public void shoutsWhenCreditCardExpiryDateIsAbsent() {
    CreditCard cardWithoutExpiryDate = CreditCardBuilder.make()
            .withHolder("Card Without Expiry Date")
            .withIssuingBank(" Bank")
            .withValidNumber()
            .withValidCVV()
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithoutExpiryDate, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.validUntil: Expiry Date is mandatory!");
  }

  @Test
  public void shoutsWhenCreditCardCVVIsNotPresent() {
    CreditCard cardWithoutCVV = CreditCardBuilder.make()
            .withHolder("Card Without CVV")
            .withIssuingBank("Bank")
            .withValidNumber()
            .withFutureExpiryDate()
            .build();

    assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWithoutCVV, chargedAmount);
    });
  }

  @Test
  public void shoutsWhenCreditCardCVVDoesNotContain3Digits() {
    CreditCard cardWith2DigitCVV = CreditCardBuilder.make()
            .withHolder("Card With 2 Digit CVV")
            .withIssuingBank("Bank")
            .withValidNumber()
            .withFutureExpiryDate()
            .havingCVVDigits(2)
            .build();

    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(cardWith2DigitCVV, chargedAmount);
    });
    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.creditCard.cvv: must have 3 digits");
  }

  @Test
  public void shoutsWhenAmountValueIsNotPresentInCharge() {
    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(validCard, new Money(Currency.getInstance("INR"), null));
    });

    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.charged.amount: is required!");
  }

  @Test
  public void shoutsWhenCurrencyIsNotPresentInCharge() {
    Throwable validationException = assertThrows(ValidationException.class, () -> {
      service.verifyTransactionAuthenticity(validCard, new Money(null, 123.45d));
    });

    assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.charged.currency: is required!");
  }

}
