package com.tsys.fraud_checker.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreditCardBuilder {
  private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
  private final Random random = new Random(87654321L);

  private Boolean isExpiryDateInFuture = null;
  private String issuingBankName;
  private String holderName;
  private String number;
  private Integer cvv;
  private Date validUntil;

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
    this.isExpiryDateInFuture = true;
    return this;
  }

  public CreditCardBuilder withPastExpiryDate() {
    this.isExpiryDateInFuture = false;
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
            .map(String::valueOf)
            .collect(Collectors.joining()));
    return this;
  }

  public CreditCardBuilder withExpiryDate(String date) {
    try {
      validUntil = sdf.parse(date);
    } catch (ParseException e) {
      validUntil = null;
    }
    return this;
  }

  public CreditCard build() {
    return Optional.ofNullable(isExpiryDateInFuture)
            .map(isFutureDate -> {
              try {
                return isFutureDate ? sdf.parse("30-DEC-4000") : Date.from(Instant.EPOCH);
              } catch (ParseException e) {
                return null;
              }
            })
            .map(expiryDate -> new CreditCard(number, holderName, issuingBankName, expiryDate, cvv))
            .orElse(new CreditCard(number, holderName, issuingBankName, validUntil, cvv));
  }
}
