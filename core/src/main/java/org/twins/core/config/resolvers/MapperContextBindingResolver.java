package org.twins.core.config.resolvers;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.twins.core.controller.rest.annotation.MapperModeParam;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Component
public class MapperContextBindingResolver implements HandlerMethodArgumentResolver {

    private static final String LAZY_RELATION = "lazyRelation";

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(MapperContext.class);
    }


    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        MapperContext mapperContext = new MapperContext();
        Parameter[] parameters = parameter.getMethod().getParameters();
        for (Parameter param : parameters) {
            if (param.isAnnotationPresent(MapperModeParam.class) || param.isAnnotationPresent(RequestParam.class) && MapperMode.class.isAssignableFrom(param.getType())) {
                Object value = webRequest.getParameter(param.getName());
                if (value != null) {
                    setModeInMapperContext(mapperContext, value, param.getType());
                }
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                switch (param.getName()) {
                    case LAZY_RELATION:
                        Object value = webRequest.getParameter(param.getName());
                        if (value != null) {
                            mapperContext.setLazyRelations(Boolean.parseBoolean((String) value));
                        }
                        break;
                }
            }
        }
        return mapperContext;
    }

    private void setModeInMapperContext(MapperContext mapperContext, Object value, Class<?> paramType) {
        mapperContext.setMode((MapperMode) Enum.valueOf((Class<Enum>) paramType, (String) value));
    }

}
