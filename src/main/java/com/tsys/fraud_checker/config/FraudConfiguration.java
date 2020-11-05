package com.tsys.fraud_checker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Random;

@Configuration
@PropertySource("classpath:application-${spring.profiles.active:development}.properties")
public class FraudConfiguration {
  @Bean
  public Random random() {
     return new Random(4864325435L);
  }
}
