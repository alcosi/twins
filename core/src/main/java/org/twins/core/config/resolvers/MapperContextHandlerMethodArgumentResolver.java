package org.twins.core.config.resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.service.MapperModesResolveService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperContextHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final MapperModesResolveService mapperModesResolveService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the method parameter has MapperContextBinding annotation
        return parameter.hasParameterAnnotation(MapperContextBinding.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, @Nullable NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        MapperContext mapperContext = new MapperContext();
        MapperContextBinding contextBinding = parameter.getParameterAnnotation(MapperContextBinding.class);
        if (null != contextBinding && null != webRequest) {
            // Retrieve root mapper classes and blocked mappers from the annotation
            Class<? extends RestDTOMapper<?, ?>>[] rootMapperClasses = contextBinding.roots();
            Set<Class<? extends MapperMode>> blockedModes = new HashSet<>(Set.of(contextBinding.block()));

            // Handle lazyRelation parameter if the response type inherits from ResponseRelatedObjectsDTOv1
            if (ResponseRelatedObjectsDTOv1.class.isAssignableFrom(contextBinding.response())) {
                String lazyRelationValue = webRequest.getParameter("lazyRelation");
                mapperContext.setLazyRelations(lazyRelationValue == null || Boolean.parseBoolean(lazyRelationValue));
            }

            // Iterate over each root mapper class to get parameters
            for (Class<? extends RestDTOMapper<?, ?>> rootMapperClass : rootMapperClasses) {
                Map<String, Class<? extends Enum<?>>> boundMapperModes = mapperModesResolveService.getModesFromMapper(rootMapperClass);

                // For each parameter, get its value from the request and set it in the mapper context
                for (Map.Entry<String, Class<? extends Enum<?>>> boundMapperMode : boundMapperModes.entrySet()) {
                    if (!blockedModes.contains(boundMapperMode.getValue())) {
                        String paramValue = webRequest.getParameter(boundMapperMode.getKey());
                        Class<? extends Enum<?>> enumType = boundMapperMode.getValue();

                        // Resolve the enum value from request parameter or use default if not provided
                        Enum<?> enumValue = paramValue != null ?
                                Enum.valueOf(enumType.asSubclass(Enum.class), paramValue) :
                                enumType.getEnumConstants()[0]; // Default to the first enum constant

                        if (enumValue instanceof MapperMode) {
                            mapperContext.setMode((MapperMode) enumValue);
                        }
                    } else
                        log.info("{} is blocked for controller", boundMapperMode.getKey());
                }
            }
        }
        return mapperContext;
    }
}
