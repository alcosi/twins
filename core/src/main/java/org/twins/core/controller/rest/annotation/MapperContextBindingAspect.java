package org.twins.core.controller.rest.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;

import java.lang.reflect.Parameter;

//@Aspect
@Deprecated
@Component
public class MapperContextBindingAspect {

    @Before("@annotation(MapperContextBinding)")
    public void bindMapperContext(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        Object[] args = joinPoint.getArgs();
        MapperContext mapperContext = null;
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].getType().equals(MapperContext.class)) {
                if (args[i] == null) args[i] = new MapperContext();
                mapperContext = (MapperContext) args[i];
                break;
            }
        if(null != mapperContext) {
            for (int i = 0; i < parameters.length; i++) {
                if ((parameters[i].isAnnotationPresent(MapperModeParam.class) || parameters[i].isAnnotationPresent(RequestParam.class)) && !parameters[i].getName().equals("lazyRelation")) {
                    setModeInMapperContext(mapperContext, args[i]);
                } else if (parameters[i].isAnnotationPresent(RequestParam.class) && parameters[i].getName().equals("lazyRelation")) { //TODO just one
                    mapperContext.setLazyRelations((Boolean) args[i]);
                }
            }
        }
    }

    private void setModeInMapperContext(MapperContext mapperContext, Object value) {
        if (value instanceof MapperMode) {
            mapperContext.setMode((MapperMode) value);
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + value.getClass().getName());
        }
    }
}


