package com.tsys.fraud_checker.web.internal;

import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.web.FraudCheckPayload;

public class FraudCheckStub implements StubProvider<FraudCheckPayload, FraudStatus> {
    public final FraudCheckPayload request;
    public final FraudStatus response;

    public FraudCheckStub(FraudCheckPayload request, FraudStatus response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public FraudCheckPayload getRequest() {
        return request;
    }

    @Override
    public FraudStatus getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "FraudCheckStub{" +
                "request=" + request.toString() +
                ", response=" + response.toString() +
                '}';
    }
}
