package org.twins.core.service;

import org.springframework.stereotype.Service;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.MapperMode;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class DynamicMapperParameterService {

    // Cache for storing mapper parameters to avoid repetitive reflection calls
    private final Map<Class<? extends RestDTOMapper>, Map<String, Class<? extends Enum<?>>>> mapperParameterCache = new HashMap<>();

    // Retrieve parameters from the mapper class, either from cache or by extracting
    public Map<String, Class<? extends Enum<?>>> getParametersFromMapper(Class<? extends RestDTOMapper> mapperClass, Set<Class<?>> blocked) {
        if (mapperParameterCache.containsKey(mapperClass)) return mapperParameterCache.get(mapperClass);
        Map<String, Class<? extends Enum<?>>> parameters = new HashMap<>();
        scanMapper(mapperClass, parameters, new HashSet<>(), blocked, new HashSet<>());
        Map<String, Class<? extends Enum<?>>> sortedParameters = new TreeMap<>(parameters);
        mapperParameterCache.put(mapperClass, sortedParameters);
        return sortedParameters;
    }


    // Recursively scan mappers and their fields for annotations
    private void scanMapper(Class<? extends RestDTOMapper> mapperClass, Map<String, Class<? extends Enum<?>>> parameters, Set<Class<? extends RestDTOMapper>> visited, Set<Class<?>> blocked, Set<Class<? extends MapperMode>> parentModes) {
        // Avoid infinite recursion
        if (visited.contains(mapperClass) || blocked.contains(mapperClass)) {
            return;
        } else visited.add(mapperClass);

        // Check for class-level MapperModeBinding annotation if no parent mode pointers are present
        if (mapperClass.isAnnotationPresent(MapperModeBinding.class)) {
            MapperModeBinding classAnnotation = mapperClass.getAnnotation(MapperModeBinding.class);
            for (Class<? extends MapperMode> mode : classAnnotation.modes()) {
                if(!parentModes.isEmpty()) {
                    boolean foundPointer = false;
                    for (Class<? extends MapperMode> parentMode : parentModes) {
                        if (isPointerMode(parentMode, mode)) {
                            foundPointer = true;
                            break;
                        }
                    }
                    if(!foundPointer) parameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
                } else {
                    parameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
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
                if (field.isAnnotationPresent(MapperModePointerBinding.class) || field.isAnnotationPresent(MapperModeBinding.class)) {
                    if (field.isAnnotationPresent(MapperModePointerBinding.class)) {
                        MapperModePointerBinding fieldAnnotation = field.getAnnotation(MapperModePointerBinding.class);
                        for (Class<? extends MapperMode> mode : fieldAnnotation.modes()) {
                            parameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
                            newParentModes.add(mode);
                        }
                    }
                    if (field.isAnnotationPresent(MapperModeBinding.class)) {
                        MapperModeBinding fieldAnnotation = field.getAnnotation(MapperModeBinding.class);
                        for (Class<? extends MapperMode> mode : fieldAnnotation.modes()) {
                            parameters.put("show" + mode.getSimpleName(), (Class<? extends Enum<?>>) mode);
                        }
                    }
                }
                scanMapper(fieldMapperClass, parameters, visited, blocked, newParentModes);
            }
        }
    }

    private boolean isPointerMode(Class<? extends MapperMode> pointCandidate, Class<? extends MapperMode> mode) {
        if (MapperModePointer.class.isAssignableFrom(pointCandidate))
            for (MapperModePointer<?> instance : (MapperModePointer<?>[]) pointCandidate.getEnumConstants())
                if (instance.point().getClass().equals(mode))
                    return true;
        return false;
    }
}
