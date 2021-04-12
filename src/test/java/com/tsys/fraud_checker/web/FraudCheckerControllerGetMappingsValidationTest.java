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

// For Junit4, use @RunWith
// @RunWith(MockitoJUnitRunner.class)
// For Junit5, use @ExtendWith
@ExtendWith(MockitoExtension.class)
// Here we are using MockMVC in standalone mode, hence not loading any context. 
//
// NOTE: No Web-Server is deployed
@Tags({
        @Tag("StandAlone"),
        @Tag("UnitTest")
})
public class FraudCheckerControllerGetMappingsValidationTest {

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
    public void validatesPathVariableIdAtOrAboveValue5() throws Exception {
        final var request = givenARequestFor("/validatePathVariable/5");
        final ResultActions resultActions = whenTheRequestIsMade(request);
        thenExpect(resultActions,
                MockMvcResultMatchers.status().isOk(),
                MockMvcResultMatchers.content().bytes("valid".getBytes()));
    }

    @Test
    public void shoutsWhenPathVariableIdIsBelow5() throws Exception {
        final var request = givenARequestFor("/validatePathVariable/4");
        final ResultActions resultActions = whenTheRequestIsMade(request);
        final var response = "{\n" +
                "    \"validationErrors\": [\n" +
                "        {\n" +
                "            \"fieldName\": \"validatePathVariable.id\",\n" +
                "            \"message\": \"A minimum value of 5 is required\"\n" +
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
    public void shoutsWhenPathVariableIdIsAbove9999() throws Exception {
        final var request = givenARequestFor("/validatePathVariable/10000");
        final ResultActions resultActions = whenTheRequestIsMade(request);
        final var response = "{\n" +
                "    \"validationErrors\": [\n" +
                "        {\n" +
                "            \"fieldName\": \"validatePathVariable.id\",\n" +
                "            \"message\": \"A maximum value of 9999 can be given\"\n" +
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
        final var request = givenARequestFor("/validateRequestParameter?param=5");
        final ResultActions resultActions = whenTheRequestIsMade(request);
        thenExpect(resultActions,
                MockMvcResultMatchers.status().isOk(),
                MockMvcResultMatchers.content().bytes("valid".getBytes()));
    }

    @Test
    public void shoutsWhenRequestParameterIsBelow5() throws Exception {
        final var request = givenARequestFor("/validateRequestParameter?param=4");
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

    @Test
    public void shoutsWhenRequestParameterIsAbove9999() throws Exception {
        final var request = givenARequestFor("/validateRequestParameter?param=10000");
        final ResultActions resultActions = whenTheRequestIsMade(request);
        final var response = "{\n" +
                "    \"validationErrors\": [\n" +
                "        {\n" +
                "            \"fieldName\": \"validateRequestParameter.param\",\n" +
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

    private MockHttpServletRequestBuilder givenARequestFor(String url) {
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
