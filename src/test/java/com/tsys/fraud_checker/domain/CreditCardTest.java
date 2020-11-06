package com.tsys.fraud_checker.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreditCardTest {

  @Test
  public void isValidBeforeExpiryDate() {
    final var creditCard = CreditCardBuilder.make()
                    .withFutureExpiryDate()
                    .build();

    assertThat(creditCard.hasExpired()).isTrue();
  }

  @Test
  public void isInvalidAfterExpiryDate() {
    final var creditCard = CreditCardBuilder.make()
            .withPastExpiryDate()
            .build();

    assertThat(creditCard.hasExpired()).isFalse();
  }
}
