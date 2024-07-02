package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.MapperMode;

import java.lang.reflect.Field;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DynamicMapperParameterService {

    private final ApplicationContext applicationContext;

    // Cache for storing mapper parameters to avoid repetitive reflection calls
    private static final Map<Class<? extends RestDTOMapper>, Map<String, Class<? extends Enum<?>>>> MAPPER_PARAMETERS_CACHE = new HashMap<>();

    // Retrieve parameters from the mapper class, either from cache or by extracting
    public Map<String, Class<? extends Enum<?>>> getParametersFromMapper(Class<? extends RestDTOMapper> mapperClass, Set<Class<?>> blocked) {
        if (MAPPER_PARAMETERS_CACHE.containsKey(mapperClass)) return MAPPER_PARAMETERS_CACHE.get(mapperClass);
        Map<String, Class<? extends Enum<?>>> parameters = new HashMap<>();
        scanMapper(mapperClass, parameters, new HashSet<>(), blocked, new HashSet<>());
        MAPPER_PARAMETERS_CACHE.put(mapperClass, parameters);
        return parameters;
    }


    // Recursively scan mappers and their fields for annotations
    private void scanMapper(Class<? extends RestDTOMapper> mapperClass, Map<String, Class<? extends Enum<?>>> parameters, Set<Class<? extends RestDTOMapper>> visited, Set<Class<?>> blocked, Set<Class<? extends MapperMode>> parentModes) {
        // Avoid infinite recursion
        if (visited.contains(mapperClass)) return;
        visited.add(mapperClass);

        // Check cache
        if (MAPPER_PARAMETERS_CACHE.containsKey(mapperClass)) {
            parameters.putAll(MAPPER_PARAMETERS_CACHE.get(mapperClass));
            return;
        }
        // Temporary map to store parameters for the current mapper class
        Map<String, Class<? extends Enum<?>>> tempParameters = new HashMap<>();

        // Check for class-level MapperModeBinding annotation if no parent mode pointers are present
        if (mapperClass.isAnnotationPresent(MapperModeBinding.class)) {
            MapperModeBinding classAnnotation = mapperClass.getAnnotation(MapperModeBinding.class);
            for (Class<? extends MapperMode> mode : classAnnotation.modes()) {
                if (!blocked.contains(mode)) {
                    if (!parentModes.isEmpty()) {
                        boolean foundPointer = false;
                        for (Class<? extends MapperMode> parentMode : parentModes) {
                            if (isPointerMode(parentMode, mode)) {
                                foundPointer = true;
                                break;
                            }
                        }
                        if (!foundPointer)
                            tempParameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
                    } else {
                        tempParameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
                    }
                }
            }
        }

        // Scan fields for MapperModePointerBinding and MapperModeBinding annotations
        Field[] fields = mapperClass.getDeclaredFields();
        for (Field field : fields) {
            if (RestDTOMapper.class.isAssignableFrom(field.getType())) {
                Class<? extends RestDTOMapper> fieldMapperClass = (Class<? extends RestDTOMapper>) field.getType();
                Set<Class<? extends MapperMode>> newParentModes = new HashSet<>(parentModes);

                // Check for MapperModePointerBinding or MapperModeBinding on field
                if (field.isAnnotationPresent(MapperModePointerBinding.class)) {
                    MapperModePointerBinding fieldAnnotation = field.getAnnotation(MapperModePointerBinding.class);
                    for (Class<? extends MapperMode> mode : fieldAnnotation.modes()) {
                        if(!blocked.contains(mode)) {
                            tempParameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
                            newParentModes.add(mode);
                        }
                    }
                }
                scanMapper(fieldMapperClass, tempParameters, visited, blocked, newParentModes);
            }
        }
        MAPPER_PARAMETERS_CACHE.put(mapperClass, tempParameters);
        parameters.putAll(tempParameters);
    }

    private boolean isPointerMode(Class<? extends MapperMode> pointCandidate, Class<? extends MapperMode> mode) {
        if (MapperModePointer.class.isAssignableFrom(pointCandidate))
            for (MapperModePointer<?> instance : (MapperModePointer<?>[]) pointCandidate.getEnumConstants())
                if (instance.point().getClass().equals(mode))
                    return true;
        return false;
    }

    @PostConstruct
    private void initializeCache() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        Set<Class<? extends RestDTOMapper>> mappersToCache = new HashSet<>();

        for (Object controller : controllers.values()) {
            Class<?> controllerClass = controller.getClass();
            Field[] fields = controllerClass.getDeclaredFields();
            for (Field field : fields) {
                if (RestDTOMapper.class.isAssignableFrom(field.getType()) && !field.getType().getSimpleName().contains("Reverse")) {
                    mappersToCache.add((Class<? extends RestDTOMapper>) field.getType());
                }
            }
        }

        for (Class<? extends RestDTOMapper> mapper : mappersToCache) {
            getParametersFromMapper(mapper, Collections.emptySet());
        }
        MAPPER_PARAMETERS_CACHE.entrySet().forEach(System.out::println);
    }
}
