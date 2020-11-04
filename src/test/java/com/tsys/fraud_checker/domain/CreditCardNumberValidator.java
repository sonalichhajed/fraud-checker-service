package com.tsys.fraud_checker.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class CreditCardNumberValidator {
  private String regex = "^(?:(?<visa>4[0-9]{12}(?:[0-9]{3})?)|" +
          "(?<mastercard>5[1-5][0-9]{14})|" +
          "(?<discover>6(?:011|5[0-9]{2})[0-9]{12})|" +
          "(?<amex>3[47][0-9]{13})|" +
          "(?<diners>3(?:0[0-5]|[68][0-9])?[0-9]{11})|" +
          "(?<jcb>(?:2131|1800|35[0-9]{3})[0-9]{11}))$";

  private Pattern pattern = Pattern.compile(regex);

  /**
   * https://howtodoinjava.com/java/regex/java-regex-validate-credit-card-numbers/
   * 1. Valid Credit Card Numbers Formats
   * ====================================
   * On an actual credit cardNumber, the digits of the embossed cardNumber number are usually placed into groups of four. That makes the cardNumber number easier for humans to read. Each of the credit cardNumber companies uses this number format.
   * <p>
   * We’ll exploit that difference of formats between each company to allow users to enter a number without specifying a company. The company can be determined from the number. The format for each company is:
   * <p>
   * Visa : 13 or 16 digits, starting with 4.
   * MasterCard : 16 digits, starting with 51 through 55.
   * Discover : 16 digits, starting with 6011 or 65.
   * American Express : 15 digits, starting with 34 or 37.
   * Diners Club : 14 digits, starting with 300 through 305, 36, or 38.
   * JCB : 15 digits, starting with 2131 or 1800, or 16 digits starting with 35.
   * <p>
   * With spaces and hyphens stripped from the input, the next regular
   * expression checks if the credit cardNumber number uses the format of any of
   * the six major credit cardNumber companies. It uses named capture to detect
   * which brand of credit cardNumber the customer has.
   * <p>
   * If you don’t need to determine which type the cardNumber is, you can
   * remove the six capturing groups that surround the pattern for each
   * cardNumber type, as they don’t serve any other purpose.
   * <p>
   * If you accept only certain brands of credit cards, you can delete
   * the cards that you don’t accept from the regular expression.
   * For example, when deleting JCB, make sure to delete the last
   * remaining “|” in the regular expression as well. If you end up
   * with “|” in your regular expression, it will accept the empty
   * string as a valid cardNumber number as well.
   * <p>
   * Regex : ^(?:(?<visa>4[0-9]{12}(?:[0-9]{3})?)|
   * (?<mastercard>5[1-5][0-9]{14})|
   * (?<discover>6(?:011|5[0-9]{2})[0-9]{12})|
   * (?<amex>3[47][0-9]{13})|
   * (?<diners>3(?:0[0-5]|[68][0-9])?[0-9]{11})|
   * (?<jcb>(?:2131|1800|35[0-9]{3})[0-9]{11}))$
   *
   * @param cardNumber
   */
  public boolean isValidCardNumberFormat(String cardNumber) {
    System.out.println("========================================================");
    // Match the cardNumber
    Matcher matcher = pattern.matcher(cardNumber);
    System.out.println(String.format("For cardNumber %s, Match found? %s ", cardNumber, matcher.matches()));
    return matcher.matches();

//    if (matcher.matches()) {
//      //If cardNumber is valid then verify which group it belong
//      System.out.println("master cardNumber? = " + matcher.group("mastercard"));
//      System.out.println("visa cardNumber? = " + matcher.group("visa"));
//      System.out.println("discover cardNumber? = " + matcher.group("discover"));
//      System.out.println("amex cardNumber? = " + matcher.group("amex"));
//      System.out.println("diners cardNumber? = " + matcher.group("diners"));
//      System.out.println("jcb cardNumber? = " + matcher.group("jcb"));
//    }
  }


  public boolean isChecksumValid(String cardNumber) {
    String cardNumberReversed = new StringBuilder(cardNumber).reverse().toString();
    Map<Boolean, List<Integer>> oddEvens = IntStream.range(1, cardNumber.length() + 1)
            .mapToObj(i -> Arrays.asList(Integer.parseInt(String.valueOf(cardNumberReversed.charAt(i-1))), i))
            .collect(partitioningBy(x -> x.get(1) % 2 == 0, mapping(x -> x.get(0), toList())));

    int s1 = oddEvens.get(false).stream().mapToInt(Integer::intValue).sum();
    int s2 = oddEvens.get(true).stream().mapToInt(Integer::intValue)
            .map(x -> 2 * x)
            .map(x -> (x > 9) ? (x % 10) + 1 : x).sum();
    return String.valueOf(s1 + s2).endsWith("0");
  }

  public static void main(String[] args) {
    List<String> cards = List.of(
            "4386-4367-8899-1009",
            "5431 4367 8899 1009",
            "9567-7889-1234-9999",
            "2621195162335",
            "49927398716",
            "1234567812345670",
            "4485284720134093",
            "49927398717",
            "1234567812345678");

    CreditCardNumberValidator validator = new CreditCardNumberValidator();
    cards.stream()
            // Strip all hyphens and spaces
            .map(card -> card.replaceAll("[-| ]", ""))
            .filter(validator::isValidCardNumberFormat)
            .filter(validator::isChecksumValid)
            .forEach(card -> {
              System.out.println(String.format("Valid Card %s", card));
            });

    System.out.println("validator.isChecksumValid(\"49927398716\") = " + validator.isChecksumValid("49927398716"));

  }
}
