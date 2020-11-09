package com.tsys.fraud_checker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.Random;

@Configuration
@PropertySource("classpath:application-${spring.profiles.active:development}.properties")
public class FraudConfiguration {
  @Bean
  public Random random() {
    return new Random(4864325435L);
  }

}
