package com.tsys.fraud_checker.web.internal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Profile("development")
@Component
public class Stubs {
    private final Map<String, StubProvider> stubbedResponses = new HashMap<>();


    public <REQUEST, RESPONSE> void put(String url, StubProvider<REQUEST, RESPONSE> stubProvider) {
        stubbedResponses.put(url, stubProvider);
    }

    public <REQUEST, RESPONSE> Optional<StubProvider<REQUEST, RESPONSE>> get(String url) {
        return Optional.ofNullable(stubbedResponses.get(url));
    }

    public Map<String, StubProvider> getAllStubs() {
        return new HashMap<>(stubbedResponses);
    }

    @Override
    public String toString() {
        return "Stubs{" +
                "stubbedResponses=" + stubbedResponses +
                '}';
    }
}
