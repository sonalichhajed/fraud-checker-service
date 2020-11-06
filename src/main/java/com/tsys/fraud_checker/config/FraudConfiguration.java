package com.tsys.fraud_checker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Random;

@Configuration
@PropertySource("classpath:application-${spring.profiles.active:development}.properties")
public class FraudConfiguration {
  @Bean
  public Random random() {
    return new Random(4864325435L);
  }

  // After adding the dependency to build.gradle, add the Docket bean.
  // The configuration of Swagger is based on the Docket bean
  // That's all:
  // 1. check the URL: http://localhost:9001/v2/api-docs
  //    for swagger api docs JSON format
  // 2. For Swagger UI point the browser to:
  //    http://localhost:9001/swagger-ui/index.html
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
            // select() method returns an instance of ApiSelectorBuilder,
            // which provides a way to control the endpoints exposed by Swagger.
            .select()
            // configure predicates for selecting RequestHandlers with the help
            // of RequestHandlerSelectors and PathSelectors.
            //
            // Using any() for both will make documentation for our entire
            // API available through Swagger.
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build();
  }

}
