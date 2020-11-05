package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.services.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.logging.Logger;


// NOTE:
// =====
// Here I've not used @RestController as I want to display HTML
// page as well.  But at the same time, I want few of the methods
// to return non-HTML response, hence we annotate each method with
// @ResponseBody.
// In case, if there was no requirement to show HTML page, then
// this needs to be annotated with @RestController and not
// @Controller.  Once annotated with @RestController, you don't
// need @ResponseBody annotation.

@Controller
@RequestMapping("/")
public class FraudCheckerController {

    private static final Logger LOG = Logger.getLogger(FraudCheckerController.class.getName());

    private final VerificationService verificationService;

    @Autowired
    public FraudCheckerController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @RequestMapping
    public String index() {
        return "index.html";
    }

    @GetMapping("ping")
    @ResponseBody
    public ResponseEntity<String> pong() {
        return ResponseEntity.ok(String.format("{ 'PONG' : '%s is running fine!' }", FraudCheckerController.class.getSimpleName()));
    }

    /**
     * https://reflectoring.io/bean-validation-with-spring-boot/
     * Bean Validation works by defining constraints to the fields
     * of a class by annotating them with certain annotations.
     *
     * Then, you pass an object of that class into a Validator
     * which checks if the constraints are satisfied.
     *
     * There are three things we can validate for any incoming HTTP request:
     * 1. the request body,
     *    @see FraudCheckerController#checkFraud(FraudCheckPayload)
     * 2. variables within the path (e.g. id in /foos/{id})
     *    @see FraudCheckerController#validatePathVariable(int)
     * 3. query parameters.
     *    @see FraudCheckerController#validateRequestParameter(int)
     *
     * Use @Valid on Complex Types
     * If the Input class contains a field with another complex type that
     * should be validated, this field, too, needs to be annotated with
     * Valid.
     *
     */
    @PostMapping(value = "check", consumes = "application/json", produces = "application/json")
    public ResponseEntity<FraudStatus> checkFraud(
            @RequestBody @Valid FraudCheckPayload payload) {
        try {
            LOG.info(() -> String.format("{ 'checkFraud' : ' for chargedAmount %s on %s'}", payload.charge, payload.creditCard));
            FraudStatus fraudStatus = verificationService.verifyTransactionAuthenticity(payload.creditCard, payload.charge);
            LOG.info(() -> String.format("{ 'FraudStatus' : '%s'}", fraudStatus));
            final var httpHeaders = new HttpHeaders() {{
                setContentType(MediaType.APPLICATION_JSON);
            }};

            return new ResponseEntity<>(fraudStatus, httpHeaders, HttpStatus.OK);
//            return ResponseEntity.ok(fraudStatus);
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validatePathVariable/{id}")
    ResponseEntity<String> validatePathVariable(
            @PathVariable("id") @Min(5) int id) {
        return ResponseEntity.ok("valid");
    }

    @GetMapping("/validateRequestParameter")
    ResponseEntity<String> validateRequestParameter(
            @RequestParam("param") @Min(5) int param) {
        return ResponseEntity.ok("valid");
    }
}
