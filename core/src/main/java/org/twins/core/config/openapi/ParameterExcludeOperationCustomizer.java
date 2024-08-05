package org.twins.core.config.openapi;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParameterExcludeOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            List<Parameter> newParameters = parameters.stream()
                    .filter(this::isNotExcludeParam)
                    .collect(Collectors.toList());
            operation.setParameters(newParameters);
        }
        return operation;
    }

    private boolean isNotExcludeParam(Parameter parameter) {
        String parameterName = parameter.getName();
        return !(parameterName != null && (parameterName.equals("mapperContext") || parameterName.equals("pagination")));
    }
}
