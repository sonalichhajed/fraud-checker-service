package com.tsys.fraud_checker.services;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.domain.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Profile("development")
@Component
public class VerificationServiceRouter implements VerificationService {

    public enum RouteTo { ACTUAL, STUB };

    private final DefaultVerificationService defaultVerificationService;
    private final StubbedDelayVerificationService stubbedDelayVerificationService;

    public RouteTo routeTo = RouteTo.ACTUAL;

    private static final Logger LOG = Logger.getLogger(VerificationServiceRouter.class.getName());

    @Autowired
    public VerificationServiceRouter(DefaultVerificationService defaultVerificationService,
                                     StubbedDelayVerificationService stubbedDelayVerificationService) {
        this.defaultVerificationService = defaultVerificationService;
        this.stubbedDelayVerificationService = stubbedDelayVerificationService;
    }

    public FraudStatus verifyTransactionAuthenticity(CreditCard card, Money charge) throws InterruptedException {
        LOG.info(String.format("Routing to %s", routeTo));
        if (routeTo == RouteTo.ACTUAL)
            return defaultVerificationService.verifyTransactionAuthenticity(card, charge);
        else
            return stubbedDelayVerificationService.verifyTransactionAuthenticity(card, charge);
    }
}
