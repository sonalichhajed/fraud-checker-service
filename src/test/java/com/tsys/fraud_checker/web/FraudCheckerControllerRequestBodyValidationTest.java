package com.tsys.fraud_checker.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.domain.Money;
import com.tsys.fraud_checker.services.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import static org.mockito.BDDMockito.given;

// For Junit4, use @RunWith
// @RunWith(SpringRunner.class)
// For Junit5, use @ExtendWith
@ExtendWith(SpringExtension.class)
// We're only testing the web layer, we use the @WebMvcTest
// annotation. It allows us to easily test requests and responses
// using the set of static methods implemented by the
// MockMvcRequestBuilders and MockMvcResultMatchers classes.
@WebMvcTest(FraudCheckerController.class)
@AutoConfigureMockMvc
// We can verify the validation behavior with an integration test:
public class FraudCheckerControllerRequestBodyValidationTest {

  @MockBean
  private VerificationService verificationService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
  private final Money chargedAmount = new Money(Currency.getInstance("INR"), 1235.45d);
  private CreditCard jacksCreditCard;

  @BeforeEach
  public void setupValidCreditCard() throws ParseException {
    Date future = sdf.parse("30-DEC-3000");
    jacksCreditCard = new CreditCard("1234 5678 9012 3456", "Jumping Jack", "Bank of Test", future, 123);
  }

  @Test
  public void chargingValidCreditCard() throws Exception {
    final var request = givenAFraudCheckRequestFor(jacksCreditCard, chargedAmount);
    FraudStatus ignoreSuccess = new FraudStatus(0, 0, jacksCreditCard.isValid());
    given(verificationService.verifyTransactionAuthenticity(jacksCreditCard, chargedAmount))
            .willReturn(ignoreSuccess);

    final ResultActions resultActions = whenTheRequestIsMade(request);
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isOk(),
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void shoutsWhenChargingWithoutAnyCreditCard() throws Exception {
    final var request = givenAFraudCheckRequestFor(null, chargedAmount);
    final ResultActions resultActions = whenTheRequestIsMade(request);
    final var response = "{\n" +
            "    \"validationErrors\": [\n" +
            "        {\n" +
            "            \"fieldName\": \"creditCard\",\n" +
            "            \"message\": \"Require Credit Card Details!\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    final var content = MockMvcResultMatchers.content();
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isBadRequest(),
            content.contentType(MediaType.APPLICATION_JSON),
            content.json(response));
  }
  @Test
  public void shoutsWhenChargingCreditCardWithoutAmount() throws Exception {
    final var request = givenAFraudCheckRequestFor(jacksCreditCard, null);
    final ResultActions resultActions = whenTheRequestIsMade(request);
    final var response = "{\n" +
            "    \"validationErrors\": [\n" +
            "        {\n" +
            "            \"fieldName\": \"chargedAmount\",\n" +
            "            \"message\": \"Charged amount must be supplied!\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    final var content = MockMvcResultMatchers.content();
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isBadRequest(),
            content.contentType(MediaType.APPLICATION_JSON),
            content.json(response));
  }

  private MockHttpServletRequestBuilder givenAFraudCheckRequestFor(CreditCard creditCard, Money money) throws JsonProcessingException {
    var input = new FraudCheckPayload(creditCard, money);
    var payload = objectMapper.writeValueAsString(input);

    return MockMvcRequestBuilders.post("/check")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(payload);
  }

  private ResultActions whenTheRequestIsMade(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request);
  }

  private void thenExpect(ResultActions resultActions, ResultMatcher ...matchers) throws Exception {
    resultActions.andExpect(ResultMatcher.matchAll(matchers));
  }
//  @Test
//  public void whenPostRequestToUsersAndInValidUser_thenCorrectResponse() throws Exception {
//    String user = "{\"name\": \"\", \"email\" : \"bob@domain.com\"}";
//    mockMvc.perform(MockMvcRequestBuilders.post("/users")
//            .content(user)
//            .contentType(MediaType.APPLICATION_JSON_UTF8))
//            .andExpect(MockMvcResultMatchers.status().isBadRequest())
//            .andExpect(MockMvcResultMatchers.jsonPath("$.name", Is.is("Name is mandatory")))
//            .andExpect(MockMvcResultMatchers.content()
//                    .contentType(MediaType.APPLICATION_JSON_UTF8));
//  }
}
