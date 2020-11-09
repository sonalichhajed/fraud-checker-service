package com.tsys.fraud_checker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tsys.fraud_checker.spring.validators.NumberOfDigits;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class CreditCard {

  @ApiModelProperty(
          value = "Card Number - Must be between 16 and 19 digits.  It can contain non-digit characters like SPACE or a dash '-'",
          name = "number",
          dataType = "String",
          required = true,
          example = "4485-2847-2013-4093")
  @NotBlank(message = "Card number is required")
  @Length(min = 16, max = 19)
  @CreditCardNumber(ignoreNonDigitCharacters = true, message = "Invalid Credit Card Number")
  @LuhnCheck(message = "Failed Luhn check!")
  public final String number;

  @NotBlank(message = "is required")
  public final String holderName;

  @NotBlank(message = "name is required")
  public final String issuingBank;

  @NotNull(message = "Expiry Date is mandatory!")
  public final Date validUntil;

  //  The @ApiParam annotation is for the parameters of an API resource request,
  //  whereas @ApiModelProperty is for properties of the model.
  @ApiModelProperty(
          value = "Card Verification Value - Must be 3 digits",
          name = "cvv",
          dataType = "integer",
          required = true,
          example = "123")
  @NotNull(message = "is mandatory!")
  @NumberOfDigits(value = 3, message = "must have 3 digits")
  public final Integer cvv;

  @Deprecated
  public CreditCard() {
    this(null, null, null, null, null);
  }

  public CreditCard(String number, String holderName, String issuingBank, Date validUntil, Integer cvv) {
    this.number = number;
    this.holderName = holderName;
    this.issuingBank = issuingBank;
    this.validUntil = validUntil;
    this.cvv = cvv;
  }

  /**
   * JSON parse error: (was java.lang.NullPointerException);
   * nested exception is com.fasterxml.jackson.databind.JsonMappingException: (was java.lang.NullPointerException)
   * (through reference chain: com.tsys.fraud_checker.web.FraudCheckPayload["creditCard"]->com.tsys.fraud_checker.domain.CreditCard["valid"])
   *
   * Solution for com.fasterxml.jackson.databind.JsonMappingException:
   * By default jackson tries to serialize the class as well as all the
   * fields of the class, so you could have got the null pointer exception.
   *
   * Add @JsonIgnore to your getter method, in our case, the getter is
   * hasExpired() method
   */
  @JsonIgnore
  public boolean hasExpired() {
    return validUntil.after(new Date());
  }

  @Override
  public String toString() {
    return "CreditCard{" +
            "number='" + number + '\'' +
            ", holderName='" + holderName + '\'' +
            ", issuingBank='" + issuingBank + '\'' +
            ", validUntil=" + validUntil +
            ", cvv=" + cvv +
            '}';
  }
}
