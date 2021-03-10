package com.tsys.fraud_checker.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("UnitTest")
public class FraudStatusTest {

    @Test
    public void overallStatusIsPassWhenCreditCardIsValid() {
        // Given
        final int validCvv = 0;
        final int validAddress = 0;
        final boolean cardExpired = false;
        final FraudStatus pass = new FraudStatus(validCvv, validAddress, cardExpired);

        // When-Then
        assertThat(pass.overall, is(FraudStatus.PASS));
    }

    @Test
    public void overallStatusIsFailWhenCreditCardHasExpired() {
        // Given
        final int validCvv = 0;
        final int validAddress = 0;
        final boolean cardExpired = true;
        final FraudStatus fail = new FraudStatus(validCvv, validAddress, cardExpired);

        // When-Then
        assertThat(fail.overall, is(FraudStatus.FAIL));
    }

    @Test
    public void overallStatusIsFailWhenCreditCardCvvIsIncorrect() {
        // Given
        final int invalidCvv = 1;
        final int validAddress = 0;
        final boolean cardExpired = false;
        final FraudStatus fail = new FraudStatus(invalidCvv, validAddress, cardExpired);

        // When-Then
        assertThat(fail.overall, is(FraudStatus.FAIL));
    }

    @Test
    public void overallStatusIsSuspiciousWhenAddressVerificationFails() {
        // Given
        final int validCvv = 0;
        final int incorrectAddress = 1;
        final boolean cardExpired = false;
        final FraudStatus suspicious = new FraudStatus(validCvv, incorrectAddress, cardExpired);

        // When-Then
        assertThat(suspicious.overall, is(FraudStatus.SUSPICIOUS));
    }
}