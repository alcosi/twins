package org.twins.core.config.resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.service.MapperModesResolveService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperContextHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final MapperModesResolveService mapperModesResolveService;
    @Lazy
    private final PermissionService permissionService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the method parameter has MapperContextBinding annotation
        return parameter.hasParameterAnnotation(MapperContextBinding.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, @Nullable NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws ServiceException {
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

                        if (enumValue instanceof MapperMode mapperMode) {
                            checkModePermissions(boundMapperMode.getKey(), mapperMode);
                            mapperContext.setMode(mapperMode);
                        }
                    } else
                        log.info("{} is blocked for controller", boundMapperMode.getKey());
                }
            }
        }
        return mapperContext;
    }

    /**
     * Declarative gate for @ProtectedBy mapper modes: denied modes fail here, at request parsing,
     * instead of blowing up later during response mapping. Same error code as the old mapper-side checks,
     * so the client-visible error contract is unchanged.
     */
    private void checkModePermissions(String paramName, MapperMode mode) throws ServiceException {
        for (ProtectedBy protection : mapperModesResolveService.getModeProtections(mode)) {
            if (!permissionService.currentUserHasPermission(protection.anyOf(), protection.value())) {
                String requiredPermissions = Arrays.stream(protection.value()).map(Permissions::name).collect(Collectors.joining(","));
                log.warn("Access denied: user does not have required permissions [{}] for {} (request param {})", requiredPermissions, mode, paramName);
                throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + mode + "] is not allowed for current user");
            }
        }
    }
}
