package com.tsys.fraud_checker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FraudCheckerApplication {

    public static void main(String[] args) {
//    System.setProperty("spring.devtools.restart.enabled", "true");
        SpringApplication.run(FraudCheckerApplication.class, args);
    }

}
