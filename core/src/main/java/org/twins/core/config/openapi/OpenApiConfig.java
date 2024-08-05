package org.twins.core.config.openapi;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.twins.core.service.MapperModesResolveService;

@Configuration
public class OpenApiConfig {

    @Autowired
    private MapperModesResolveService mapperParameterService;

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
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                .group("twins-api")
                .addOperationCustomizer(new MapperContextOperationCustomizer(mapperParameterService))
                .addOperationCustomizer(new SimplePaginationParamsOperationCustomizer())
                .addOperationCustomizer(new ParameterExcludeOperationCustomizer())
                .addOperationCustomizer(new HeadersOperationCustomizer())
                .packagesToScan("org.twins.core.controller.rest")
                .build();
    }
}
