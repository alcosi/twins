package org.twins.core.config.openapi;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.service.MapperModesResolveService;

import java.util.*;

@Component
public class MapperContextOperationCustomizer implements OperationCustomizer {

    private final MapperModesResolveService mapperParameterService;

    // Constructor for dependency injection
    @Autowired
    public MapperContextOperationCustomizer(MapperModesResolveService mapperParameterService) {
        this.mapperParameterService = mapperParameterService;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // Iterate through method parameters to find MapperContextBinding annotation and parameter of MapperContext.class
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            MapperContextBinding contextBinding = methodParameter.getParameterAnnotation(MapperContextBinding.class);
            if (contextBinding != null && methodParameter.getParameterType().equals(MapperContext.class)) {
                // Get the root mapper classes specified in the annotation
                Class<? extends RestDTOMapper<?, ?>>[] rootMapperClasses = contextBinding.roots();
                Set<Parameter> parameters = new TreeSet<>(Comparator.comparing(Parameter::getName));

                // Get blocked mappers
                Set<Class<?>> blockedMappers = new HashSet<>(Set.of(contextBinding.block()));

                // Add lazyRelation parameter if the response type inherits from ResponseRelatedObjectsDTOv1
                if (ResponseRelatedObjectsDTOv1.class.isAssignableFrom(contextBinding.response())) {
                    Parameter lazyRelationParam = new Parameter()
                            .name("lazyRelation")
                            .in("query")
                            .style(Parameter.StyleEnum.SIMPLE)
                            .schema(new Schema<>()._default(true).type("boolean"));
                    parameters.add(lazyRelationParam);
                }

                // Iterate through each root mapper class to extract parameters
                for (Class<? extends RestDTOMapper<?, ?>> rootMapperClass : rootMapperClasses) {
                    Map<String, Class<? extends Enum<?>>> boundMapperModes = mapperParameterService.getModesFromMapper(rootMapperClass);

                    // Create Swagger parameters from the extracted mapper parameters
                    for (Map.Entry<String, Class<? extends Enum<?>>> entry : boundMapperModes.entrySet()) {
                        if (!blockedMappers.contains(entry.getValue())) {
                            Parameter parameter = new Parameter()
                                    .name(entry.getKey())
                                    .in("query")
                                    .style(Parameter.StyleEnum.SIMPLE)
                                    .schema(new Schema<>()
                                            ._enum(List.of(entry.getValue().getEnumConstants()))
                                            ._default(entry.getValue().getEnumConstants()[0].name())); // Set default value to the first enum constant
                            parameters.add(parameter);
                        }
                    }
                }

                // Add parameters to the operation
                if (null == operation.getParameters()) operation.setParameters(new ArrayList<>());
                operation.getParameters().addAll(parameters);
                break;
            }
        }
        return operation;
    }
}
