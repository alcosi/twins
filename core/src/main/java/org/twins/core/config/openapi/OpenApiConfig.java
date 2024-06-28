package org.twins.core.config.openapi;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Twins API")
                                .description("Core api for twins framework")
                                .version("v1.0"))
                .externalDocs(
                        new ExternalDocumentation()
                                .description("Documentation name")
                                .url("https://example.com"));
    }

    @Bean
    public ParameterCustomizer mapperModeParamCustomizer() {
        return new MapperModeParamCustomizer();
    }

    @Bean
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                .group("twins-api")
                .addOperationCustomizer(new MapperContextParameterOperationCustomizer())
                .addOperationCustomizer(new HeadersOperationCustomizer())
                .packagesToScan("org.twins.core.controller.rest")
                .build();
    }
}
