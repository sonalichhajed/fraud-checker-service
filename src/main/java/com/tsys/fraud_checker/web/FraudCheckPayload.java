package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.Money;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * NOTE: Use @Valid on Complex Types
 * If the Input class contains a field with another complex type that
 * should be validated, this field, too, needs to be annotated with
 * Valid.
 * <p>
 * For example - In our case, we need to annotate CreditCard and Money
 * fields with Valid annotation.
 */
public class FraudCheckPayload {
    @Valid
    @NotNull(message = "Require Credit Card Details!")
    public final CreditCard creditCard;

    @Valid
    @NotNull(message = "amount must be supplied!")
    public final Money charge;

    @Deprecated
    public FraudCheckPayload() {
        this(null, null);
    }

    public FraudCheckPayload(CreditCard creditCard, Money charge) {
        this.creditCard = creditCard;
        this.charge = charge;
    }

    @Override
    public String toString() {
        return "FraudCheckPayload{" +
                "creditCard=" + creditCard +
                ", charge=" + charge +
                '}';
    }
}
