package org.twins.core.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.controller.rest.annotation.ParametersApiUserAnonymousHeaders;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.service.HttpRequestService;

import java.util.ArrayList;
import java.util.List;

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
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                .group("twins-api")
                .addOperationCustomizer((operation, handlerMethod) ->
                {
                    if (handlerMethod.hasMethodAnnotation(ParametersApiUserHeaders.class)) {
                        List<Parameter> parameters = new ArrayList<>();
                        parameters.add(headerParameterDomainId());
                        parameters.add(headerParameterAuthToken());
//                        parameters.add(headerParameterUserId());
//                        parameters.add(headerParameterBusinessAccountId());
                        parameters.add(headerParameterChannel());
                        if (operation.getParameters() != null)
                            parameters.addAll(operation.getParameters());
                        operation.setParameters(parameters);
                    }
                    if (handlerMethod.hasMethodAnnotation(ParametersApiUserAnonymousHeaders.class)) {
                        List<Parameter> parameters = new ArrayList<>();
                        parameters.add(headerParameterDomainId());
                        parameters.add(headerParameterChannel());
                        parameters.add(headerParameterLocale());
                        if (operation.getParameters() != null)
                            parameters.addAll(operation.getParameters());
                        operation.setParameters(parameters);
                    }
                    if (handlerMethod.hasMethodAnnotation(ParameterChannelHeader.class)) {
                        List<Parameter> parameters = new ArrayList<>();
                        parameters.add(headerParameterChannel());
                        if (operation.getParameters() != null)
                            parameters.addAll(operation.getParameters());
                        operation.setParameters(parameters);
                    }
//                    for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
//                        if (methodParameter.hasParameterAnnotation(ParameterPathDomainId.class)) {
//                            operation.addParametersItem(pathParameterDomainId());
//                        }
//                    }
                    return operation;
                })
                .packagesToScan("org.twins.core.controller.rest")

//                .pathsToMatch("/categories/**")
                .build();
    }

    private Parameter headerParameterUserId() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_USER_ID)
                .required(true)
                .example(DTOExamples.USER_ID);
    }

    private Parameter headerParameterDomainId() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_DOMAIN_ID)
                .required(true)
                .example(DTOExamples.DOMAIN_ID);
    }

    private Parameter headerParameterLocale() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_LOCALE)
                .required(true)
                .example(DTOExamples.LOCALE);
    }

    private Parameter headerParameterBusinessAccountId() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_BUSINESS_ACCOUNT_ID)
                .required(true)
                .example(DTOExamples.BUSINESS_ACCOUNT_ID);
    }

    private Parameter headerParameterAuthToken() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_AUTH_TOKEN)
                .required(true)
                .example(DTOExamples.AUTH_TOKEN);
    }

    private Parameter headerParameterChannel() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_CHANNEL)
                .required(true)
                .example(DTOExamples.CHANNEL);
    }

//    @Bean
//    public GroupedOpenApi expenseApi() {
//        return GroupedOpenApi.builder()
//                .group("Expense API")
//                .pathsToMatch("/expenses/**")
//                .build();
//    }

}