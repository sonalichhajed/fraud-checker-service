package com.tsys.fraud_checker.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsys.fraud_checker.config.FraudControllerAdvice;
import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.domain.Money;
import com.tsys.fraud_checker.services.VerificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

// For Junit4, use @RunWith
// @RunWith(MockitoJUnitRunner.class)
// For Junit5, use @ExtendWith
@ExtendWith(MockitoExtension.class)
// Here we are using MockMVC in standalone mode, hence not loading any context. 
//
// NOTE: No Web-Server is deployed
public class FraudCheckerControllerStandaloneUnitTest {

  @Mock
  private VerificationService verificationService;

  // Annotate our FraudCheckerController instance with @InjectMocks. So, Mockito injects the
  // mocked verificationService into the controller instead of the real bean instance.
  @InjectMocks
  private FraudCheckerController fraudCheckerController;

  private MockMvc mockMvc;

  @BeforeEach
  public void buildMockMvc(){
    // MockMvc standalone approach
    mockMvc = MockMvcBuilders.standaloneSetup(fraudCheckerController)
    //  Add custom Advices and Filters manually and control each
    //        .setControllerAdvice(new FraudControllerAdvice())
    //       .addFilters(new FraudCheckerFilter())
            .build();
  }

  @Test
  public void health() throws Exception {
    final var request = givenRequestFor("/ping", false);
    final ResultActions resultActions = whenTheRequestIsMade(request);
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isOk(),
            MockMvcResultMatchers.content().bytes("{ \"PONG\" : \"FraudCheckerController is running fine!\" }".getBytes()));
  }

  @Test
  public void homesToIndexPage() throws Exception {
    final var request = givenRequestFor("/", false);
    final ResultActions resultActions = whenTheRequestIsMade(request);
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void chargingAValidCard() throws Exception {
    final var request = givenRequestFor("/check", true)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\n" +
                    "    \"creditCard\" : {\n" +
                    "        \"number\": \"4485 2847 2013 4093\",\n" +
                    "        \"holderName\" : \"Jumping Jack\",\n" +
                    "        \"issuingBank\" : \"Bank of America\",\n" +
                    "        \"validUntil\" : \"2020-10-04T01:00:26.874+00:00\",\n" +
                    "        \"cvv\" : 123\n" +
                    "    },\n" +
                    "    \"charge\" : {\n" +
                    "        \"currency\" : \"INR\",\n" +
                    "        \"amount\" : 1235.45\n" +
                    "    }\n" +
                    "}");
    FraudStatus ignoreSuccess = new FraudStatus(0, 0, false);
    given(verificationService.verifyTransactionAuthenticity(any(CreditCard.class), any(Money.class)))
            .willReturn(ignoreSuccess);

    final ResultActions resultActions = whenTheRequestIsMade(request);
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isOk(),
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.content().json(convertObjectToJsonString(ignoreSuccess))
    );
  }

  private String convertObjectToJsonString(Object value) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    //Converting the Object to JSONString
    return mapper.writeValueAsString(value);
  }

  @Test
  public void shoutsWhenThereIsAProblemWithCheckingCardFraud() throws Exception {
    given(verificationService.verifyTransactionAuthenticity(any(CreditCard.class), any(Money.class)))
            .willThrow(new InterruptedException());

    final var request = givenRequestFor("/check", true)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\n" +
                    "    \"creditCard\" : {\n" +
                    "        \"number\": \"4485-2847-2013-4093\",\n" +
                    "        \"holderName\" : \"Jumping Jack\",\n" +
                    "        \"issuingBank\" : \"Bank of America\",\n" +
                    "        \"validUntil\" : \"2020-10-04T01:00:26.874+00:00\",\n" +
                    "        \"cvv\" : 123\n" +
                    "    },\n" +
                    "    \"charge\" : {\n" +
                    "        \"currency\" : \"INR\",\n" +
                    "        \"amount\" : 1235.45\n" +
                    "    }\n" +
                    "}");

    final ResultActions resultActions = whenTheRequestIsMade(request);

    thenExpect(resultActions,
            MockMvcResultMatchers.status().isInternalServerError());
  }

  private MockHttpServletRequestBuilder givenRequestFor(String url, boolean isPostRequest) {
    if (isPostRequest)
      return MockMvcRequestBuilders.post(url).characterEncoding("UTF-8");

    return MockMvcRequestBuilders.get(url).characterEncoding("UTF-8");
  }

  private ResultActions whenTheRequestIsMade(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request);
  }

  private void thenExpect(ResultActions resultActions, ResultMatcher... matchers) throws Exception {
    resultActions.andExpect(ResultMatcher.matchAll(matchers));
  }
}
