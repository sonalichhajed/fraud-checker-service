package com.tsys.fraud_checker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Configuration
public class SwaggerConfiguration {
  // After adding the dependency to build.gradle, add the Docket bean.
  // The configuration of Swagger is based on the Docket bean
  // That's all:
  // 1. check the URL: http://localhost:9001/v2/api-docs
  //    for swagger api docs JSON format
  // 2. For Swagger UI point the browser to:
  //    http://localhost:9001/swagger-ui/index.html
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.OAS_30)
        // select() method returns an instance of ApiSelectorBuilder,
        // which provides a way to control the endpoints exposed by Swagger.
        .select()
        // configure predicates for selecting RequestHandlers with the help
        // of RequestHandlerSelectors and PathSelectors.
        //
        // Using any() for both will make documentation for our entire
        // API available through Swagger.
        .apis(RequestHandlerSelectors.any())
        // Exclude Spring BasicErrorController from the path
        .paths(Predicate.not(PathSelectors.regex("/error.*")))
        .build()
        .apiInfo(apiInfo())
        .produces(Set.of("application/json"))
        .consumes(Set.of("application/json"));
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "FraudChecker REST API",
        "Checks for Credit Card Frauds.",
        "API v1.0",
        "https://www.gnu.org/licenses/gpl-3.0.html",
        new Contact("Dhaval Dalal", "https://dhavaldalal.wordpress.com", "dhaval@dalal.com"),
        "Copyleft License", "https://en.wikipedia.org/wiki/Copyleft", Collections.emptyList());
  }
}
