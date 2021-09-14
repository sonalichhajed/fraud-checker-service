package com.tsys.fraud_checker.web;

import com.tsys.fraud_checker.domain.FraudStatus;
import com.tsys.fraud_checker.services.DefaultVerificationService;
import com.tsys.fraud_checker.services.VerificationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Max;
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
//@Qualifier("verificationService")
public class FraudCheckerController {

    private static final Logger LOG = Logger.getLogger(FraudCheckerController.class.getName());

    private final VerificationService verificationService;

    @Autowired
    public FraudCheckerController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @ApiIgnore
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String index() {
        return "index.html";
    }

    @ApiOperation(value = "Am I alive?", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Got Health status", response = String.class),
//            @ApiResponse(code = 429, message = "Too Many Requests")
    })
    @GetMapping(value = "ping", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> pong() {
        return ResponseEntity.ok(String.format("{ \"PONG\" : \"%s is running fine!\" }", FraudCheckerController.class.getSimpleName()));
    }
    
    @ApiOperation(value = "Am I healthy?", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Got Health status", response = String.class),
//            @ApiResponse(code = 429, message = "Too Many Requests")
    })
    @GetMapping(value = "health", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(String.format("{ \"Status\" : \"%s is running OK!\" }", FraudCheckerController.class.getSimpleName()));
    }

    @ApiOperation(value = "Validate Path Variable", produces = "text/plain")
    // The @ApiParam annotation is for the parameters of an API resource request,
    //  whereas @ApiModelProperty is for properties of the model.
    @GetMapping("validatePathVariable/{id}")
    ResponseEntity<String> validatePathVariable(
            @PathVariable("id")
            @Min(value = 5, message = "A minimum value of 5 is required")
            @Max(value = 9999, message = "A maximum value of 9999 can be given")
            @ApiParam(
                    name = "id",
                    type = "int",
                    value = "a number",
                    example = "1",
                    required = true)
                    int id) {
        LOG.info(() -> String.format("validatePathVariable(), Got id = %d", id));
        return ResponseEntity.ok("valid");
    }

    @ApiOperation(value = "Validate Request Parameter", produces = "text/plain")
    @GetMapping("validateRequestParameter")
    ResponseEntity<String> validateRequestParameter(
            @RequestParam("param")
            @Min(5) @Max(9999) int param) {
        LOG.info(() -> String.format("validateRequestParameter(), Got param = %d", param));
        return ResponseEntity.ok("valid");
    }

    @ApiOperation(value = "Validate Header Parameter", produces = "text/plain")
    @GetMapping("validateHeader")
    ResponseEntity<String> validateHeader(
            @RequestHeader("param")
            @Min(5) @Max(9999) int param) {
        LOG.info(() -> String.format("validateHeader(), Got param = %d", param));
        return ResponseEntity.ok("valid");
    }

    @ApiOperation(value = "Validate Header Parameter Via Post", produces = "text/plain")
    @PostMapping("validateHeaderUsingPost")
    ResponseEntity<String> validateHeaderUsingPost(
            @RequestHeader(value = "param")
            @Min(5) @Max(9999) int param,
            @RequestBody @Valid FraudCheckPayload fraudCheckPayload) {
        LOG.info(() -> String.format("validateHeaderUsingPost(), Got param = %d", param));
        return ResponseEntity.ok("valid");
    }

    /**
     * https://reflectoring.io/bean-validation-with-spring-boot/
     * Bean Validation works by defining constraints to the fields
     * of a class by annotating them with certain annotations.
     * <p>
     * Then, you pass an object of that class into a Validator
     * which checks if the constraints are satisfied.
     * <p>
     * There are three things we can validate for any incoming HTTP request:
     * 1. the request body,
     *
     * @see FraudCheckerController#checkFraud(FraudCheckPayload)
     * 2. variables within the path (e.g. id in /foos/{id})
     * @see FraudCheckerController#validatePathVariable(int)
     * 3. query parameters.
     * @see FraudCheckerController#validateRequestParameter(int)
     * <p>
     * Use @Valid on Complex Types
     * If the Input class contains a field with another complex type that
     * should be validated, this field, too, needs to be annotated with
     * Valid.
     */
    @ApiOperation(value = "Check possibility of a fradulent transaction and return a status to the caller.", consumes = "application/json", produces = "application/json", response = FraudStatus.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Got Fraud Status for the check", response = FraudStatus.class),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
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
        } catch (InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
