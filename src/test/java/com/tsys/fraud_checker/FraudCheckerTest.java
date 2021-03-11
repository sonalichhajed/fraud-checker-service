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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Currency;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
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
        final ResponseEntity<String> response = client.getForEntity("/", String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void fraudCheckPasses() {
        // Given
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(0) // for CVV status PASS
                .willReturn(0); // for AddressVerification status PASS

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(validCard, chargedAmount), FraudStatus.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.PASS));
    }

    @Test
    public void fraudCheckFailsForInvalidCvv() {
        // Given
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(1) // for CVV status FAIL
                .willReturn(0); // for AddressVerification status PASS

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(validCard, chargedAmount), FraudStatus.class);
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
                .willReturn(0) // for CVV status PASS
                .willReturn(0); // for AddressVerification status PASS

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(expiredCard, chargedAmount), FraudStatus.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.FAIL));
    }

    @Test
    public void fraudCheckIsSuspicious() {
        // Given
        given(random.nextInt(anyInt()))
                .willReturn(-2000) // for sleepMillis
                .willReturn(0) // for CVV status PASS
                .willReturn(1); // for AddressVerification status FAIL

        // When
        final ResponseEntity<FraudStatus> response = client.postForEntity("/check", new FraudCheckPayload(validCard, chargedAmount), FraudStatus.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final FraudStatus fraudStatus = response.getBody();
        assertThat(fraudStatus.overall, is(FraudStatus.SUSPICIOUS));
    }
}
