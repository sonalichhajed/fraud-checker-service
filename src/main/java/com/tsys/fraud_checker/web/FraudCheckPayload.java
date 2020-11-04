package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.Money;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FraudCheckPayload {
//  @Valid
  @NotNull(message = "Require Credit Card Details!")
  public final CreditCard creditCard;

//  @Valid
  @NotNull(message = "Charged amount must be supplied!")
  public final Money chargedAmount;

  public FraudCheckPayload() {
    this(null, null);
  }

  public FraudCheckPayload(CreditCard creditCard, Money chargedAmount) {
    this.creditCard = creditCard;
    this.chargedAmount = chargedAmount;
  }
}
