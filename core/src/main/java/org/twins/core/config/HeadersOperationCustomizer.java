package org.twins.core.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;
import org.twins.core.controller.rest.annotation.*;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.service.HttpRequestService;

import java.util.ArrayList;
import java.util.List;

public class HeadersOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (handlerMethod.hasMethodAnnotation(ParametersApiUserHeaders.class)) {
            List<Parameter> parameters = new ArrayList<>();
            parameters.add(headerParameterDomainId());
            parameters.add(headerParameterAuthToken());
            parameters.add(headerParameterChannel());
            if (operation.getParameters() != null) {
                parameters.addAll(operation.getParameters());
            }
            operation.setParameters(parameters);
        }
        if (handlerMethod.hasMethodAnnotation(ParametersApiUserNoDomainHeaders.class)) {
            List<Parameter> parameters = new ArrayList<>();
            parameters.add(headerParameterAuthToken());
            parameters.add(headerParameterChannel());
            if (operation.getParameters() != null) {
                parameters.addAll(operation.getParameters());
            }
            operation.setParameters(parameters);
        }
        if (handlerMethod.hasMethodAnnotation(ParametersApiUserAnonymousHeaders.class)) {
            List<Parameter> parameters = new ArrayList<>();
            parameters.add(headerParameterDomainId());
            parameters.add(headerParameterChannel());
            parameters.add(headerParameterLocale());
            if (operation.getParameters() != null) {
                parameters.addAll(operation.getParameters());
            }
            operation.setParameters(parameters);
        }
        if (handlerMethod.hasMethodAnnotation(ParameterChannelHeader.class)) {
            List<Parameter> parameters = new ArrayList<>();
            parameters.add(headerParameterChannel());
            if (operation.getParameters() != null) {
                parameters.addAll(operation.getParameters());
            }
            operation.setParameters(parameters);
        }
        if (handlerMethod.hasMethodAnnotation(ParameterDomainHeader.class)) {
            List<Parameter> parameters = new ArrayList<>();
            parameters.add(headerParameterDomainId());
            parameters.add(headerParameterChannel());
            if (operation.getParameters() != null) {
                parameters.addAll(operation.getParameters());
            }
            operation.setParameters(parameters);
        }
        return operation;
    }

    private Parameter headerParameterUserId() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_USER_ID)
                .required(true)
                .schema(new StringSchema())
                .example(DTOExamples.USER_ID);
    }

    private Parameter headerParameterDomainId() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_DOMAIN_ID)
                .required(true)
                .schema(new StringSchema())
                .example(DTOExamples.DOMAIN_ID);
    }

    private Parameter headerParameterLocale() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_LOCALE)
                .required(true)
                .schema(new StringSchema())
                .example(DTOExamples.LOCALE);
    }

    private Parameter headerParameterBusinessAccountId() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_BUSINESS_ACCOUNT_ID)
                .required(true)
                .schema(new StringSchema())
                .example(DTOExamples.BUSINESS_ACCOUNT_ID);
    }

    private Parameter headerParameterAuthToken() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_AUTH_TOKEN)
                .required(true)
                .schema(new StringSchema())
                .example(DTOExamples.AUTH_TOKEN);
    }

    private Parameter headerParameterChannel() {
        return new HeaderParameter()
                .name(HttpRequestService.HEADER_CHANNEL)
                .required(true)
                .schema(new StringSchema())
                .example(DTOExamples.CHANNEL);
    }
}
