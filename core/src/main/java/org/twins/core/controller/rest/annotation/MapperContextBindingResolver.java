package org.twins.core.controller.rest.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;

import java.lang.reflect.Parameter;

//@Component
public class MapperContextBindingResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(MapperContext.class);
    }


    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        MapperContext mapperContext = null;
        if (parameter.getMethod().isAnnotationPresent(MapperContextBinding.class)) {
            System.out.println("ra+");
            Parameter[] parameters = parameter.getMethod().getParameters();
            for (Parameter param : parameters) {
                if (param.getType().equals(MapperContext.class)) {
                    System.out.println("context found");
                    mapperContext = new MapperContext();
                    break;
                }
            }

            if (mapperContext != null) {
                for (Parameter param : parameters) {
                    System.out.println(param.getName());
                    if (param.isAnnotationPresent(MapperModeParam.class) || param.isAnnotationPresent(RequestParam.class) && !param.getName().equals("lazyRelation")) {
                        Object value = webRequest.getParameter(param.getName());
                        if (value != null) {
                            System.out.println("here");
                            setModeInMapperContext(mapperContext, value);
                        }
                    } else if (param.isAnnotationPresent(RequestParam.class) && param.getName().equals("lazyRelation")) {
                        Object value = webRequest.getParameter(param.getName());
                        System.out.println(value);
                        if (value != null) {
                            mapperContext.setLazyRelations(Boolean.parseBoolean((String) value));
                        }
                    }
                }
            }
        }
        return mapperContext;
    }

    private void setModeInMapperContext(MapperContext mapperContext, Object value) {
        System.out.println("set: " + value);
        if (value instanceof MapperMode) {
            mapperContext.setMode((MapperMode) value);
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + value.getClass().getName());
        }
    }

}
