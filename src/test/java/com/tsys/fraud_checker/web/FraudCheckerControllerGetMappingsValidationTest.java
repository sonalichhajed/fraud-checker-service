package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.services.VerificationService;
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
// We verify the validation behavior with an integration test:
public class FraudCheckerControllerGetMappingsValidationTest {

  @MockBean
  private VerificationService verificationService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void validatesPathVariableIdAtOrAboveValue5() throws Exception {
    final var request = givenAFraudCheckRequestFor("/validatePathVariable/5");
    final ResultActions resultActions = whenTheRequestIsMade(request);
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isOk(),
            MockMvcResultMatchers.content().bytes("valid".getBytes()));
  }

  @Test
  public void shoutsWhenPathVariableIdIsBelow5() throws Exception {
    final var request = givenAFraudCheckRequestFor("/validatePathVariable/4");
    final ResultActions resultActions = whenTheRequestIsMade(request);
    final var response = "{\n" +
            "    \"validationErrors\": [\n" +
            "        {\n" +
            "            \"fieldName\": \"validatePathVariable.id\",\n" +
            "            \"message\": \"must be greater than or equal to 5\"\n" +
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
  public void validatesRequestParameterAtOrAboveValue5() throws Exception {
    final var request = givenAFraudCheckRequestFor("/validateRequestParameter?param=5");
    final ResultActions resultActions = whenTheRequestIsMade(request);
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isOk(),
            MockMvcResultMatchers.content().bytes("valid".getBytes()));
  }

  @Test
  public void shoutsWhenRequestParameterIsBelow5() throws Exception {
    final var request = givenAFraudCheckRequestFor("/validateRequestParameter?param=4");
    final ResultActions resultActions = whenTheRequestIsMade(request);
    final var response = "{\n" +
            "    \"validationErrors\": [\n" +
            "        {\n" +
            "            \"fieldName\": \"validateRequestParameter.param\",\n" +
            "            \"message\": \"must be greater than or equal to 5\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    final var content = MockMvcResultMatchers.content();
    thenExpect(resultActions,
            MockMvcResultMatchers.status().isBadRequest(),
            content.contentType(MediaType.APPLICATION_JSON),
            content.json(response));
  }

  private MockHttpServletRequestBuilder givenAFraudCheckRequestFor(String url) {
    return MockMvcRequestBuilders.get(url)
            .characterEncoding("UTF-8");
  }

  private ResultActions whenTheRequestIsMade(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request);
  }

  private void thenExpect(ResultActions resultActions, ResultMatcher... matchers) throws Exception {
    resultActions.andExpect(ResultMatcher.matchAll(matchers));
  }
}
