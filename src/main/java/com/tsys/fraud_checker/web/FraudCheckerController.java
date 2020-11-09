package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.services.VerificationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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

// NOTE:
// =====
// We have to add Spring’s @Validated annotation to the controller
// at class level to tell Spring to evaluate the constraint annotations
// on method parameters.
//
// The @Validated annotation is only evaluated on class level in this
// case, even though it’s allowed to be used on methods
//
// This was introduced, because the two GetMappings
// 1. validatePathVariable
// 2. validateRequestParameter
// needed it.
//
// It was not necessary when earlier had only the PostMapping
// 1. checkFraud
// It was enough to have @Valid annotation on the method
// parameter FraudCheckPayload.
@Validated
@RequestMapping("/")
public class FraudCheckerController {

    private static final Logger LOG = Logger.getLogger(FraudCheckerController.class.getName());

    private final VerificationService verificationService;

    @Autowired
    public FraudCheckerController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @ApiOperation(value = "Show index page.")
    @RequestMapping
    public String index() {
        return "index.html";
    }

    @ApiOperation(value = "Am I alive?")
    @GetMapping("ping")
    @ResponseBody
    public ResponseEntity<String> pong() {
        return ResponseEntity.ok(String.format("{ 'PONG' : '%s is running fine!' }", FraudCheckerController.class.getSimpleName()));
    }
    // The @ApiParam annotation is for the parameters of an API resource request,
    //  whereas @ApiModelProperty is for properties of the model.
    @GetMapping("validatePathVariable/{id}")
    ResponseEntity<String> validatePathVariable(
            @PathVariable("id")
            @Min(value = 5, message = "A minimum value of 5 is required")
            @ApiParam(
                    name =  "id",
                    type = "int",
                    value = "a number",
                    example = "1",
                    required = true)
                    int id) {
        LOG.info(() -> String.format("validatePathVariable(), Got id = %d", id));
        return ResponseEntity.ok("valid");
    }

    @GetMapping("validateRequestParameter")
    ResponseEntity<String> validateRequestParameter(
            @RequestParam("param")
            @Min(5) int param) {
        LOG.info(() -> String.format("validateRequestParameter(), Got param = %d", param));
        return ResponseEntity.ok("valid");
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
    @ApiOperation(value = "Check possibility of a fradulent transaction and return a status to the caller.")
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
}
