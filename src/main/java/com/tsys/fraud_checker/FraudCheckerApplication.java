package com.tsys.fraud_checker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class FraudCheckerApplication {

	public static void main(String[] args) {
//    System.setProperty("spring.devtools.restart.enabled", "true");
		SpringApplication.run(FraudCheckerApplication.class, args);
	}

}
