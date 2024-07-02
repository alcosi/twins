package org.twins.core.config.resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.twins.core.service.DynamicMapperParameterService;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.MapperMode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DynamicParamsHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final DynamicMapperParameterService mapperParameterService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the method parameter has MapperContextBinding annotation
        return parameter.hasParameterAnnotation(MapperContextBinding.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        MapperContext mapperContext = new MapperContext();
        MapperContextBinding contextBinding = parameter.getParameterAnnotation(MapperContextBinding.class);
        if (contextBinding != null) {
            // Retrieve root mapper classes and blocked mappers from the annotation
            Class<? extends RestDTOMapper>[] rootMapperClasses = contextBinding.roots();
            Set<Class<?>> blockedMappers = new HashSet<>(Set.of(contextBinding.block()));

            // Handle lazyRelation parameter if the response type inherits from ResponseRelatedObjectsDTOv1
            if (ResponseRelatedObjectsDTOv1.class.isAssignableFrom(contextBinding.response())) {
                String lazyRelationValue = webRequest.getParameter("lazyRelation");
                mapperContext.setLazyRelations(lazyRelationValue == null || Boolean.parseBoolean(lazyRelationValue));
            }

            // Iterate over each root mapper class to get parameters
            for (Class<? extends RestDTOMapper> rootMapperClass : rootMapperClasses) {
                Map<String, Class<? extends Enum<?>>> parameters = mapperParameterService.getParametersFromMapper(rootMapperClass, blockedMappers);

                // For each parameter, get its value from the request and set it in the mapper context
                for (Map.Entry<String, Class<? extends Enum<?>>> entry : parameters.entrySet()) {
                    String paramValue = webRequest.getParameter(entry.getKey());
                    Class<? extends Enum<?>> enumType = entry.getValue();

                    // Resolve the enum value from request parameter or use default if not provided
                    Enum<?> enumValue = paramValue != null ?
                            Enum.valueOf(enumType.asSubclass(Enum.class), paramValue) :
                            enumType.getEnumConstants()[0]; // Default to the first enum constant

                    if (enumValue instanceof MapperMode) {
                        mapperContext.setMode((MapperMode) enumValue);
                    }
                }
            }
        }
        return mapperContext;
    }
}
