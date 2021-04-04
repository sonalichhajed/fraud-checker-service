package com.tsys.fraud_checker.services;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.CreditCardBuilder;
import com.tsys.fraud_checker.domain.Money;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ValidationException;
import java.util.Currency;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
// This is a Unit Test, so why can't we run this as a Standalone test using
// just Mockito?
// This is because this service uses @Validated annotation to validate methods
// marked with @Valid...this combination requires a Spring advice to be applied
// at service-level.  Either we wire it by hand (lot-of-work) or use the next
// available option where we have WebApplicationContext available and Spring does it
// for us automatically.
// So, maybe we can use @WebMvcTest.

//@WebMvcTest
// But that too won't work here, because when I call the service method directly, I
// don't get a wrapped validation advice (provided out-of-box by Spring)
// and hence I cannot call the service methods directly. I'd need to use MockMvc
// to hit the controller and eventually get a call on this.
//
// So how do I test this Service directly (and as a Unit Test)?  Lets go to next
// level of Unit Tests in Spring that gives us WebApplicationContext, but still
// does not load the Web-Server by using @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//
// Using this I directly auto-wire the service in the test and I also get it
// wrapped in the Spring's default validation advice.
//
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Tag("UnitTest")
// Here’s a validation test at the service level
public class DefaultVerificationServiceValidationTest {

    private static final int CVV_STATUS_PASS = 0;
    private static final int ADDRESS_VERIFICATION_STATUS_PASS = 0;
    private final Money chargedAmount = new Money(Currency.getInstance("INR"), 1235.45d);
    private final CreditCard validCard = CreditCardBuilder.make()
            .withHolder("Jumping Jack")
            .withIssuingBank("Bank of Test")
            .withValidNumber()
            .withValidCVV()
            .withFutureExpiryDate()
            .build();
    @MockBean
    private Random random;
    @Autowired
    private DefaultVerificationService defaultVerificationService;

    @Test
    public void isValid() throws InterruptedException {
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(CVV_STATUS_PASS)
                .willReturn(ADDRESS_VERIFICATION_STATUS_PASS);

        final var fraudStatus = defaultVerificationService.verifyTransactionAuthenticity(validCard, chargedAmount);
        assertThat(fraudStatus).isNotNull();
    }

    @Test
    public void shoutsWhenCardIsNotPresent() {
        assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(null, chargedAmount));
    }

    @Test
    public void shoutsWhenAmountIsNotPresent() {
        assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(null, null));
    }

    @Test
    public void shoutsWhenCardNumberIsAbsent() {
        CreditCard cardWithoutNumber = CreditCardBuilder.make()
                .withHolder("Card Holder")
                .withIssuingBank("Bank")
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutNumber, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.number: Card number is required");
    }

    @Test
    public void shoutsWhenCardNumberIsEmpty() {
        CreditCard cardWithoutNumber = CreditCardBuilder.make()
                .withHolder("Card Holder")
                .withIssuingBank("Bank")
                .withNumber("")
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutNumber, chargedAmount));
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: Card number is required");
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: Invalid Credit Card Number");
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: Failed Luhn check!");
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: length must be between 16 and 19");
    }

    @Test
    public void shoutsWhenCardNumberIsInvalid() throws Exception {
        var cardWithoutNumber = CreditCardBuilder.make()
                .withInvalidNumber()
                .withHolder("Card Holder")
                .withIssuingBank("Bank")
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutNumber, chargedAmount));
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: Invalid Credit Card Number");
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: Failed Luhn check!");
    }

    @Test
    public void shoutsWhenCardNumberIsOfInsufficientLength() throws Exception {
        var cardWithoutNumber = CreditCardBuilder.make()
                .withNumber("4992 7398 716")
                .withHolder("Card Holder")
                .withIssuingBank("Bank")
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutNumber, chargedAmount));
        assertThat(validationException.getMessage()).contains("verifyTransactionAuthenticity.card.number: length must be between 16 and 19");
    }

    @Test
    public void shoutsWhenCardHolderIsAbsent() {
        CreditCard cardWithoutHolder = CreditCardBuilder.make()
                .withValidNumber()
                .withIssuingBank("Bank")
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutHolder, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.holderName: is required");
    }

    @Test
    public void shoutsWhenCardHolderIsEmpty() {
        CreditCard cardWithEmptyHolder = CreditCardBuilder.make()
                .withHolder("")
                .withValidNumber()
                .withIssuingBank("Bank")
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithEmptyHolder, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.holderName: is required");
    }

    @Test
    public void shoutsWhenCardIssuingBankIsAbsent() {
        CreditCard cardWithoutBank = CreditCardBuilder.make()
                .withHolder("Card Without Bank")
                .withValidNumber()
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutBank, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.issuingBank: name is required");
    }

    @Test
    public void shoutsWhenCardIssuingBankIsEmpty() {
        CreditCard cardEmptyBank = CreditCardBuilder.make()
                .withHolder("Card Without Bank")
                .withIssuingBank("")
                .withValidNumber()
                .withFutureExpiryDate()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardEmptyBank, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.issuingBank: name is required");
    }

    @Test
    public void shoutsWhenCardExpiryDateIsAbsent() {
        CreditCard cardWithoutExpiryDate = CreditCardBuilder.make()
                .withHolder("Card Without Expiry Date")
                .withIssuingBank(" Bank")
                .withValidNumber()
                .withValidCVV()
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutExpiryDate, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.validUntil: Expiry Date is mandatory!");
    }

    @Test
    public void shoutsWhenCardCVVIsNotPresent() {
        CreditCard cardWithoutCVV = CreditCardBuilder.make()
                .withHolder("Card Without CVV")
                .withIssuingBank("Bank")
                .withValidNumber()
                .withFutureExpiryDate()
                .build();

        assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWithoutCVV, chargedAmount));
    }

    @Test
    public void shoutsWhenCardCVVDoesNotContain3Digits() {
        CreditCard cardWith2DigitCVV = CreditCardBuilder.make()
                .withHolder("Card With 2 Digit CVV")
                .withIssuingBank("Bank")
                .withValidNumber()
                .withFutureExpiryDate()
                .havingCVVDigits(2)
                .build();

        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(cardWith2DigitCVV, chargedAmount));
        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.card.cvv: must have 3 digits");
    }

    @Test
    public void shoutsWhenAmountValueIsNotPresentInCharge() {
        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(validCard, new Money(Currency.getInstance("INR"), null)));

        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.charge.amount: is required!");
    }

    @Test
    public void shoutsWhenCurrencyIsNotPresentInCharge() {
        Throwable validationException = assertThrows(ValidationException.class, () -> defaultVerificationService.verifyTransactionAuthenticity(validCard, new Money(null, 123.45d)));

        assertThat(validationException.getMessage()).isEqualTo("verifyTransactionAuthenticity.charge.currency: is required!");
    }
}
