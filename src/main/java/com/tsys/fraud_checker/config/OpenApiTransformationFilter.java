package com.tsys.fraud_checker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.oas.web.OpenApiTransformationContext;
import springfox.documentation.oas.web.WebMvcOpenApiTransformationFilter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

// 42-crunch complains of additionalProperties set to true is not good.
// This attribute is default to true and what is needed is to set it as false.
// So, we need to write a TransformationFilter to set
// additionalProperties to false for each component of you specification
// if you are using Springfox.
//
// https://stackoverflow.com/questions/64489812/how-to-set-additional-properties-to-boolean
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class OpenApiTransformationFilter implements WebMvcOpenApiTransformationFilter {
    public boolean supports(@NotNull DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
    }

    @Override
    public OpenAPI transform(OpenApiTransformationContext<HttpServletRequest> context) {
        OpenAPI openApi = context.getSpecification();
        openApi.getComponents()
                .getSchemas()
                .values()
                .forEach(schema -> schema.setAdditionalProperties(false));

        // Based on 42 Crunch recommendation 429 needs to be added to all reponses
        // Here is the code.
        openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(o -> o.getResponses().put("429", new ApiResponse() {{
                    setDescription("Too Many Requests");
                }}));

        return openApi;
    }
}