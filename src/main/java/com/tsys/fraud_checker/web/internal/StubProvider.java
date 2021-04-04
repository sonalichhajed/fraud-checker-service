package com.tsys.fraud_checker.web.internal;

public interface StubProvider<REQUEST, RESPONSE> {
    REQUEST getRequest();
    RESPONSE getResponse();
}
