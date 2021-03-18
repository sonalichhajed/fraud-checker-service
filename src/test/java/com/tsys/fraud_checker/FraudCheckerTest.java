package com.tsys.fraud_checker;

import com.tsys.fraud_checker.domain.CreditCard;
import com.tsys.fraud_checker.domain.CreditCardBuilder;
import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.domain.Money;
import com.tsys.fraud_checker.web.FraudCheckPayload;
import com.tsys.fraud_checker.web.FraudCheckerController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

// For Junit4, use @RunWith
// @RunWith(SpringRunner.class)
// For Junit5, use @ExtendWith
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tags({
        @Tag("In-Process"),
        @Tag("ComponentTest")
})
public class FraudCheckerTest {

    private final Money chargedAmount = new Money(Currency.getInstance("INR"), 1235.45d);
    private final CreditCard validCard = CreditCardBuilder.make()
            .withHolder("Jumping Jack")
            .withIssuingBank("Bank of Test")
            .withValidNumber()
            .withValidCVV()
            .withFutureExpiryDate()
            .build();

    @MockBean
    private Random random;

    @Autowired
    private FraudCheckerController fraudCheckerController;

    @Autowired
    private TestRestTemplate client;

    private static final int CVV_STATUS_PASS = 0;
    private static final int CVV_STATUS_FAIL = 1;
    private static final int ADDRESS_VERIFICATION_STATUS_PASS = 0;
    private static final int ADDRESS_VERIFICATION_STATUS_FAIL = 1;

    @Test
    public void health() {
        // Given-When
        final ResponseEntity<String> response = client.getForEntity("/ping", String.class);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("{ \"PONG\" : \"FraudCheckerController is running fine!\" }"));
    }

    @Test
    public void homesToIndexPage() {
        // Given-When
        final ResponseEntity<String> indexPage = client.getForEntity("/", String.class);

        // Then
        assertThat(indexPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(indexPage.getHeaders().getContentType(), is(new MediaType("text", "html", Charset.forName("UTF-8"))));
        final String body = indexPage.getBody();
        assertThat(body, org.hamcrest.Matchers.startsWith("<!DOCTYPE html>"));
        assertThat(body, containsString("Fraud Checker Service"));
    }

    @Test
    public void fraudCheckPasses() {
        // Given
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(CVV_STATUS_PASS)
                .willReturn(ADDRESS_VERIFICATION_STATUS_PASS);

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(validCard, chargedAmount), FraudStatus.class);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.PASS));
    }

    @Test
    public void fraudCheckFailsForInvalidCvv() {
        // Given
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(CVV_STATUS_FAIL)
                .willReturn(ADDRESS_VERIFICATION_STATUS_PASS);

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(validCard, chargedAmount), FraudStatus.class);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.FAIL));
    }

    @Test
    public void fraudCheckFailsForExpiredCard() {
        // Given
        final var expiredCard = CreditCardBuilder.make()
                .withHolder("Jumping Jack")
                .withIssuingBank("Bank of Test")
                .withValidNumber()
                .withValidCVV()
                .withPastExpiryDate()
                .build();

        System.out.println("expiredCard = " + expiredCard);
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(CVV_STATUS_PASS)
                .willReturn(ADDRESS_VERIFICATION_STATUS_PASS);

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(expiredCard, chargedAmount), FraudStatus.class);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.FAIL));
    }

    @Test
    public void fraudCheckIsSuspicious() {
        // Given
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(CVV_STATUS_PASS)
                .willReturn(ADDRESS_VERIFICATION_STATUS_FAIL);

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(validCard, chargedAmount), FraudStatus.class);

        // Then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.SUSPICIOUS));
    }
}
