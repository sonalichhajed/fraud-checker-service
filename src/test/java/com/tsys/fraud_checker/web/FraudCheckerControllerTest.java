package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.services.DefaultVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
public class FraudCheckerControllerTest {

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
                MockMvcResultMatchers.content().bytes("{ \"PONG\" : \"FraudCheckerController is running fine! Green deployment\" }".getBytes()));
    }

    @Test
    public void homesToIndexPage() throws Exception {
        final var request = givenRequestFor("/", false);
        final ResultActions resultActions = whenTheRequestIsMade(request);
        thenExpect(resultActions,
                MockMvcResultMatchers.status().isOk());
    }

    private MockHttpServletRequestBuilder givenRequestFor(String url, boolean isPostRequest) {
        final MockHttpServletRequestBuilder builder =
                isPostRequest ? MockMvcRequestBuilders.post(url)
                        : MockMvcRequestBuilders.get(url);
        return builder.characterEncoding("UTF-8");
    }

    private ResultActions whenTheRequestIsMade(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private void thenExpect(ResultActions resultActions, ResultMatcher... matchers) throws Exception {
        resultActions.andExpect(ResultMatcher.matchAll(matchers));
    }
}
