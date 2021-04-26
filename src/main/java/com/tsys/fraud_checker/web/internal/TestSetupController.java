package com.tsys.fraud_checker.web.internal;

import com.tsys.fraud_checker.services.StubbedDelayVerificationService;
import com.tsys.fraud_checker.services.VerificationServiceRouter;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.logging.Logger;

import static com.tsys.fraud_checker.services.VerificationServiceRouter.RouteTo.ACTUAL;
import static com.tsys.fraud_checker.services.VerificationServiceRouter.RouteTo.STUB;

@Profile("development")
@Controller
@RequestMapping("/setup")
public class TestSetupController {

    private final StubbedDelayVerificationService stubbedDelayVerificationService;
    private final Stubs stubs;
    private final VerificationServiceRouter verificationServiceRouter;
    private static final Logger LOG = Logger.getLogger(TestSetupController.class.getName());

    @Autowired
    public TestSetupController(StubbedDelayVerificationService stubbedDelayVerificationService,
                               Stubs stubs, VerificationServiceRouter verificationServiceRouter) {
        this.stubbedDelayVerificationService = stubbedDelayVerificationService;
        this.stubs = stubs;
        this.verificationServiceRouter = verificationServiceRouter;
    }

    @ApiOperation(value = "Am I alive?", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Got Health status", response = String.class),
    })
    @GetMapping(value = "ping", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> pong() {
        return ResponseEntity.ok(String.format("{ \"PONG\" : \"%s is running fine!\" }", getClass().getSimpleName()));
    }


    // url for setting up stubbed response for a url with payload
    @PostMapping("/stubFor/check")
    ResponseEntity<String> stub(@RequestBody FraudCheckStub fraudCheckStub) {
        final var url = "/check";
        LOG.info(() -> String.format("Setting stub for url = %s, stub = %s", url, fraudCheckStub));
        stubs.put(url, fraudCheckStub);
        LOG.info("Stubs = " + stubs);
        return ResponseEntity.ok(String.format("{ \"url\" : %s, \"stub\" : \"%s\" }", url, fraudCheckStub));
    }

    @GetMapping(path = "/getStubs", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Stubs> stubs() {
        return ResponseEntity.ok(stubs);
    }

    // url for setting up stubbed delay
    @GetMapping("/fraudCheckDelay")
    public ResponseEntity<Void> fraudCheckDelay(@RequestParam("respondIn") int timeInMillis) {
        LOG.info(() -> String.format("Setting Delay to respond from VerificationService for %d", timeInMillis));
        stubbedDelayVerificationService.setDelay(timeInMillis);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // url for turning stubResponses on/off
    @GetMapping("/stubbingFor/check")
    public ResponseEntity<Void> turnStubbingForFraudCheck(@RequestParam("on") boolean isEnabled) {
        if (isEnabled) {
            verificationServiceRouter.routeTo = STUB;
        } else {
            verificationServiceRouter.routeTo = ACTUAL;
        }
        LOG.info(String.format("Stubbing for Fraud Check is now turned %s, will route to %s", isEnabled ? "ON" : "OFF",
                verificationServiceRouter.routeTo));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}