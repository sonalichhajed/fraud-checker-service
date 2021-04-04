package com.tsys.fraud_checker.services;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Profile("development")
@Component
public class VerificationServiceRoutingInterceptor implements MethodInterceptor {
    private static final Logger LOG = Logger.getLogger(VerificationServiceRoutingInterceptor.class.getName());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        LOG.info("Invoking method..." + invocation.getMethod().getName());
        final Object result = invocation.proceed();
        LOG.info("Result of Invocation = " +  result);
        return result;
    }
}
