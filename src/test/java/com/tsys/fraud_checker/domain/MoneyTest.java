package com.tsys.fraud_checker.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tags({
        @Tag("StandAlone"),
        @Tag("UnitTest")
})
public class MoneyTest {
    private final Money inr_100_253 = new Money(Currency.getInstance("INR"), 100.253);
    private final Money usd5 = new Money(Currency.getInstance("USD"), 5d);

    @Test
    public void stringRepresentedBySymbolWithAmount2HavingPlacesOfDecimal() {
        assertThat(inr_100_253.toString(), is("₹ 100.25"));
    }

    @Test
    public void addsTwoValuesHavingSameCurrency() {
        assertThat(inr_100_253.add(inr_100_253), is(new Money(Currency.getInstance("INR"), 200.506)));
    }

    @Test
    public void shoutsWhenAddingDifferentCurrencies() {
        assertThrows(IllegalArgumentException.class,
                () -> inr_100_253.add(usd5),
                "For addition the currencies must be same!");
    }

    @Test
    public void equality() {
        assertThat(inr_100_253.equals(inr_100_253), is(true));
        assertThat(inr_100_253.equals(new Money(Currency.getInstance("INR"), 100.253)), is(true));
        assertThat(inr_100_253.equals(null), is(false));
        assertThat(inr_100_253.equals(usd5), is(false));
    }
}
