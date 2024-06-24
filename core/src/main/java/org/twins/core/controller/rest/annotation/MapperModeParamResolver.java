package org.twins.core.controller.rest.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class MapperModeParamResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MapperModeParam.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        MapperModeParam annotation = parameter.getParameterAnnotation(MapperModeParam.class);
        if (annotation != null) {
            String name = parameter.getParameterName();
            if (name == null) throw new IllegalArgumentException("Parameter name is null.");

            String defaultValue = annotation.def();
            String value = webRequest.getParameter(name);

            if (value == null || value.isEmpty()) {
                if (defaultValue.isEmpty()) {
                    Class<?> paramType = parameter.getParameterType();
                    if (paramType.isEnum()) {
                        Object[] enumConstants = paramType.getEnumConstants();
                        if (enumConstants != null && enumConstants.length > 0) {
                            defaultValue = enumConstants[0].toString();
                        } else {
                            throw new IllegalArgumentException("Enum type " + paramType.getName() + " has no constants.");
                        }
                    }
                }
                value = defaultValue;
            }

            Class<?> paramType = parameter.getParameterType();
            if (paramType.isEnum()) {
                try {
                    return Enum.valueOf((Class<Enum>) paramType, value);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid value for enum type " + paramType.getName() + ": " + value, e);
                }
            }
        }
        return null;
    }
}
