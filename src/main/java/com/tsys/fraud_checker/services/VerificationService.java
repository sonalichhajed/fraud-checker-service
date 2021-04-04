package com.tsys.fraud_checker.services;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.domain.Money;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface VerificationService {
    FraudStatus verifyTransactionAuthenticity(@NotNull @Valid CreditCard card,
                                              @NotNull @Valid Money charge) throws InterruptedException;
}
