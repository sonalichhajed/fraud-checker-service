package com.tsys.fraud_checker.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreditCardBuilder {
  private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
  private final Random random = new Random(87654321L);

  private Optional<Boolean> isExpiryDateInFuture = Optional.empty();
  private String issuingBankName;
  private String holderName;
  private String number;
  private Integer cvv;

  private CreditCardBuilder() {
  }

  public static CreditCardBuilder make() {
    return new CreditCardBuilder();
  }

  public CreditCardBuilder withNumber(String number) {
    this.number = number;
    return this;
  }

  public CreditCardBuilder withValidNumber() {
    this.number = "4485-2847-2013-4093";
    return this;
  }

  public CreditCardBuilder withInvalidNumber() {
    this.number = "1234 5678 9012 3456";
    return this;
  }

  public CreditCardBuilder withFutureExpiryDate() {
    this.isExpiryDateInFuture = Optional.of(true);
    return this;
  }

  public CreditCardBuilder withPastExpiryDate() {
    this.isExpiryDateInFuture = Optional.of(false);
    return this;
  }

  public CreditCardBuilder withIssuingBank(String name) {
    issuingBankName = name;
    return this;
  }

  public CreditCardBuilder withHolder(String name) {
    holderName = name;
    return this;
  }

  public CreditCardBuilder withCVV(Integer cvv) {
    this.cvv = cvv;
    return this;
  }

  public CreditCardBuilder withValidCVV() {
    havingCVVDigits(3);
    return this;
  }

  public CreditCardBuilder havingCVVDigits(int howMany) {
    this.cvv = Integer.parseInt(Stream.generate(() -> random.nextInt(8) + 1)
            .limit(howMany)
            .map(x -> String.valueOf(x))
            .collect(Collectors.joining()));
    return this;
  }

  public CreditCard build() {
    return isExpiryDateInFuture
            .map(value -> {
              try {
                return value ? sdf.parse("30-DEC-4000") : sdf.parse("30-DEC-2000");
              } catch (ParseException e) {
                return null;
              }
            })
            .map(validUntil -> new CreditCard(number, holderName, issuingBankName, validUntil, cvv))
            .orElse(new CreditCard(number, holderName, issuingBankName, null, cvv));
  }
}
