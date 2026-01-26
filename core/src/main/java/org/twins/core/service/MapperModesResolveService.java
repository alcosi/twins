package org.twins.core.service;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
@Slf4j
public class MapperModesResolveService {

    private final ApplicationContext applicationContext;

    // Cache for storing mapper parameters to avoid repetitive reflection calls
    private static final Map<Class<? extends RestDTOMapper<?, ?>>, Map<String, Class<? extends Enum<?>>>> MAPPER_MODES_CACHE = new HashMap<>();

    // Retrieve parameters from the mapper class, either from cache or by extracting
    public Map<String, Class<? extends Enum<?>>> getModesFromMapper(Class<? extends RestDTOMapper<?, ?>> mapperClass) {
        if (MAPPER_MODES_CACHE.containsKey(mapperClass)) return MAPPER_MODES_CACHE.get(mapperClass);
        Map<String, Class<? extends Enum<?>>> parameters = new HashMap<>();
        scanMapper(mapperClass, parameters, new HashSet<>(), new HashSet<>());
        MAPPER_MODES_CACHE.put(mapperClass, parameters);
        return parameters;
    }


    // Recursively scan mappers and their fields for annotations
    private void scanMapper(Class<? extends RestDTOMapper<?, ?>> mapperClass, Map<String, Class<? extends Enum<?>>> parameters, Set<Class<? extends RestDTOMapper<?, ?>>> visited, Set<Class<? extends MapperMode>> pointerModes) {
        // Avoid infinite recursion
        if (visited.contains(mapperClass)) return;
        visited.add(mapperClass);

//        // Check cache
//        if (MAPPER_MODES_CACHE.containsKey(mapperClass)) {
//            parameters.putAll(MAPPER_MODES_CACHE.get(mapperClass));
//            return;
//        }
//   TODO     because UserRestDTOMapper cached with 0 params on any pointer found. But sometimes we need parametrized UserRestDTOMapper.
        // Temporary map to store parameters for the current mapper class
        Map<String, Class<? extends Enum<?>>> tempParameters = new HashMap<>();

        // Check for class-level MapperModeBinding annotation if no pointer mode pointers are present
        if (mapperClass.isAnnotationPresent(MapperModeBinding.class)) {
            MapperModeBinding classAnnotation = mapperClass.getAnnotation(MapperModeBinding.class);
            for (Class<? extends MapperMode> mode : classAnnotation.modes()) {
                if (!pointerModes.isEmpty()) {
                    boolean foundPointer = false;
                    for (Class<? extends MapperMode> pointerMode : pointerModes) {
                        if (isPointerMode(pointerMode, mode)) {
                            foundPointer = true;
                            break;
                        }
                    }
                    if (!foundPointer)
                        tempParameters.put(getParameterName(mode), (Class<? extends Enum<?>>) mode);
                } else {
                    tempParameters.put(getParameterName(mode), (Class<? extends Enum<?>>) mode);
                }
            }
        }

        // Scan fields for MapperModePointerBinding and MapperModeBinding annotations
        Field[] fields = mapperClass.getDeclaredFields();
        for (Field field : fields) {
            if (RestDTOMapper.class.isAssignableFrom(field.getType())) {
                Class<? extends RestDTOMapper<?, ?>> fieldMapperClass = (Class<? extends RestDTOMapper<?, ?>>) field.getType();
                Set<Class<? extends MapperMode>> newPointerModes = new HashSet<>(pointerModes);

                // Check for MapperModePointerBinding or MapperModeBinding on field
                if (field.isAnnotationPresent(MapperModePointerBinding.class)) {
                    MapperModePointerBinding fieldAnnotation = field.getAnnotation(MapperModePointerBinding.class);
                    for (Class<? extends MapperMode> mode : fieldAnnotation.modes()) {
                        tempParameters.put(getParameterName(mode), (Class<? extends Enum<?>>) mode);
                        newPointerModes.add(mode);
                    }
                }
                scanMapper(fieldMapperClass, tempParameters, visited, newPointerModes);
            }
        }
//        if(!tempParameters.isEmpty()) MAPPER_MODES_CACHE.put(mapperClass, tempParameters);
        parameters.putAll(tempParameters);
    }

    private boolean isPointerMode(Class<? extends MapperMode> pointCandidate, Class<? extends MapperMode> mode) {
        if (MapperModePointer.class.isAssignableFrom(pointCandidate))
            for (MapperModePointer<?> instance : (MapperModePointer<?>[]) pointCandidate.getEnumConstants())
                if (instance.point().getClass().equals(mode))
                    return true;
        return false;
    }

    private static String getParameterName(Class<? extends MapperMode> mode) {
        return  "show" + mode.getSimpleName();
    }

    @PostConstruct
    private void initializeCache() {
        log.info("Mapper modes resolving started");
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        Set<Class<? extends RestDTOMapper<?, ?>>> mappersToCache = new HashSet<>();

        for (Object controller : controllers.values()) {
            Class<?> controllerClass = controller.getClass();
            Field[] fields = controllerClass.getDeclaredFields();
            for (Field field : fields) {
                if (RestDTOMapper.class.isAssignableFrom(field.getType()) && !field.getType().getSimpleName().contains("Reverse")) {
                    mappersToCache.add((Class<? extends RestDTOMapper<?, ?>>) field.getType());
                }
            }
        }

        for (Class<? extends RestDTOMapper<?,?>> mapper : mappersToCache) {
            Map<String, Class<? extends Enum<?>>> modes = getModesFromMapper(mapper);
            log.info("{} modes were resolved for {}: {}", modes.size(), mapper.getSimpleName(), StringUtils.join(modes.keySet(), ","));
        }
        log.info("Mapper modes resolving finished");
    }
}
