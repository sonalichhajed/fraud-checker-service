package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.services.DefaultVerificationService;
import com.tsys.fraud_checker.web.advices.FraudControllerAdvice;
import com.tsys.fraud_checker.web.advices.GlobalExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

@ExtendWith(MockitoExtension.class)
@Tags({
        @Tag("StandAlone"),
        @Tag("UnitTest")
})
class FraudCheckerControllerPostMappingsValidationTest {

    @Mock
    private DefaultVerificationService verificationService;
    // Annotate our FraudCheckerController instance with @InjectMocks. So, Mockito injects the
    // mocked verificationService into the controller instead of the real bean instance.
    @InjectMocks
    private FraudCheckerController fraudCheckerController;
    private MockMvc mockMvc;

    @BeforeEach
    public void buildMockMvc() {
        // MockMvc standalone approach
        final var methodValidationInterceptor = new MethodValidationInterceptor();
        final var proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.addAdvice(methodValidationInterceptor);
        proxyFactoryBean.setTarget(fraudCheckerController);
        mockMvc = MockMvcBuilders.standaloneSetup(proxyFactoryBean.getObject())
                //  Add custom Advices and Filters manually and control each
                .setControllerAdvice(new GlobalExceptionAdvice(), new FraudControllerAdvice())
                .build();
    }

    @Test
    public void validatesRequestHeaderParameterAtOrAboveValue5() throws Exception {
        final var request = givenAFraudCheckRequestFor("{\n" +
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
                "}",
                "6");

        final ResultActions resultActions = whenTheRequestIsMade(request);
        thenExpect(resultActions,
                MockMvcResultMatchers.status().isOk(),
                MockMvcResultMatchers.content().bytes("valid".getBytes()));
    }

    @Test
    public void shoutsWhenRequestHeaderParameterIsBelow5() throws Exception {
        final var request = givenAFraudCheckRequestFor("{\n" +
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
                "}",
                "4");

        final ResultActions resultActions = whenTheRequestIsMade(request);
        final var response = "{\n" +
                "    \"validationErrors\": [\n" +
                "        {\n" +
                "            \"fieldName\": \"validateHeaderUsingPost.param\",\n" +
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
    public void shoutsWhenRequestHeaderParameterIsAbove9999() throws Exception {
        final var request = givenAFraudCheckRequestFor("{\n" +
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
                "}",
                "10000");

        final ResultActions resultActions = whenTheRequestIsMade(request);
        final var response = "{\n" +
                "    \"validationErrors\": [\n" +
                "        {\n" +
                "            \"fieldName\": \"validateHeaderUsingPost.param\",\n" +
                "            \"message\": \"must be less than or equal to 9999\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        final var content = MockMvcResultMatchers.content();
        thenExpect(resultActions,
                MockMvcResultMatchers.status().isBadRequest(),
                content.contentType(MediaType.APPLICATION_JSON),
                content.json(response));
    }


    private MockHttpServletRequestBuilder givenAFraudCheckRequestFor(String requestBody, String headerParam) {
        return MockMvcRequestBuilders.post("/validateHeaderUsingPost")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(requestBody)
                .header("param", headerParam);
    }

    private ResultActions whenTheRequestIsMade(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private void thenExpect(ResultActions resultActions, ResultMatcher... matchers) throws Exception {
        resultActions.andExpect(ResultMatcher.matchAll(matchers));
    }
}