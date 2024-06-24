package org.twins.core.controller.rest.annotation;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

public class MapperModeParamCustomizer implements ParameterCustomizer {

    @Override
    public Parameter customize(Parameter parameter, MethodParameter methodParameter) {
        MapperModeParam mapperModeParamAnnotation = methodParameter.getParameterAnnotation(MapperModeParam.class);
        if (null == parameter) parameter = new Parameter();
        if (mapperModeParamAnnotation != null) {
            // Customize based on MapperModeParam annotation
            String defaultValue = mapperModeParamAnnotation.def();
            if (defaultValue.isEmpty() && methodParameter.getParameterType().isEnum()) {
                defaultValue = methodParameter.getParameterType().getEnumConstants()[0].toString();
            }

            StringSchema schema = new StringSchema();
            if (methodParameter.getParameterType().isEnum()) {
                Object[] enumConstants = methodParameter.getParameterType().getEnumConstants();
                List<String> enumValues = new ArrayList<>();
                for (Object enumConstant : enumConstants) {
                    enumValues.add(enumConstant.toString());
                }
                schema.setEnum(enumValues);
                schema.setDefault(defaultValue);
            }

            parameter.setRequired(false);
            parameter.setName(methodParameter.getParameterName());
            parameter.setSchema(schema);
        }
        return parameter;
    }


}


