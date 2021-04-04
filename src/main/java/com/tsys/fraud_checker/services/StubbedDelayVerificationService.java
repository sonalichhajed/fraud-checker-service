package com.tsys.fraud_checker.services;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.domain.Money;
import com.tsys.fraud_checker.web.FraudCheckPayload;
import com.tsys.fraud_checker.web.internal.StubProvider;
import com.tsys.fraud_checker.web.internal.Stubs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Profile("development")
@Service
public class StubbedDelayVerificationService implements VerificationService {

    private final Stubs stubs;
    private int timeInMillis;

    @Autowired
    public StubbedDelayVerificationService(Stubs stubs) {
        this.stubs = stubs;
    }

    public void setDelay(int timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    @Override
    public FraudStatus verifyTransactionAuthenticity(@NotNull @Valid CreditCard card,
                                                     @NotNull @Valid Money charge) throws InterruptedException {
        Thread.sleep(timeInMillis);
        final Optional<StubProvider<FraudCheckPayload, FraudStatus>> stubProvider = stubs.get("/check");
        return stubProvider
                .filter(sp -> {
                    final var fraudCheckPayload = sp.getRequest();
                    return fraudCheckPayload.creditCard.equals(card) && fraudCheckPayload.charge.equals(charge);
                })
                .map(StubProvider::getResponse)
                .orElseThrow(() -> new RuntimeException("No Matching Stub Found for the given request!"));
    }
}
