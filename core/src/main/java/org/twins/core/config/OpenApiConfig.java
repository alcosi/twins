package org.twins.core.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                                .title("App Title")
                                .description("App description")
                                .version("App version"))
            .externalDocs(
                new ExternalDocumentation()
                        .description("Documentation name")
                        .url("https://example.com"));
    }

    @Bean
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                .group("Category API")
                .packagesToScan("org.twins.core.controller.rest")
//                .pathsToMatch("/categories/**")
                .build();
    }

//    @Bean
//    public GroupedOpenApi expenseApi() {
//        return GroupedOpenApi.builder()
//                .group("Expense API")
//                .pathsToMatch("/expenses/**")
//                .build();
//    }

}