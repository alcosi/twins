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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModeCollection;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.service.permission.Permissions;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
@Slf4j
public class MapperModesResolveService {

    private final ApplicationContext applicationContext;

    // Cache for storing mapper parameters to avoid repetitive reflection calls
    private static final Map<Class<? extends RestDTOMapper<?, ?>>, Map<String, Class<? extends Enum<?>>>> MAPPER_MODES_CACHE = new HashMap<>();

    // Effective @ProtectedBy protections per mode constant. Filled once per mode class during mapper
    // scanning (reflection happens only there); request-time checks are plain map lookups.
    private static final Map<MapperMode, List<ProtectedBy>> MODE_PROTECTIONS = new ConcurrentHashMap<>();
    private static final Set<Class<? extends MapperMode>> PROTECTIONS_REGISTERED = ConcurrentHashMap.newKeySet();

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
                registerModeProtections(mode);
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
                        registerModeProtections(mode);
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

    /**
     * Effective @ProtectedBy protections of a resolved mode constant, precomputed during mapper scanning.
     * Empty when the mode is unprotected. No reflection happens here — only a map lookup.
     */
    public List<ProtectedBy> getModeProtections(MapperMode mode) {
        return MODE_PROTECTIONS.getOrDefault(mode, List.of());
    }

    /**
     * Resolve @ProtectedBy declarations of a request-facing mode class once and store the effective
     * protections per constant (own declaration + pointer chain / collection targets), so request-time
     * permission checks need no reflection.
     */
    private void registerModeProtections(Class<? extends MapperMode> modeClass) {
        if (!modeClass.isEnum() || !PROTECTIONS_REGISTERED.add(modeClass))
            return;
        MapperMode[] constants = modeClass.getEnumConstants();
        for (MapperMode constant : constants) {
            List<ProtectedBy> protections = new ArrayList<>();
            collectProtections(constant, protections, new HashSet<>());
            if (!protections.isEmpty())
                MODE_PROTECTIONS.put(constant, List.copyOf(protections));
        }
        // The first enum constant is the implicit default when the request param is absent.
        // A protected default would reject every request that does not pass the param explicitly — misconfiguration.
        if (constants.length > 0 && MODE_PROTECTIONS.containsKey(constants[0]))
            throw new IllegalStateException("Protected mapper mode " + modeClass.getSimpleName() + "." + ((Enum<?>) constants[0]).name() + " can not be the first (default) constant");
    }

    private void collectProtections(MapperMode mode, List<ProtectedBy> out, Set<MapperMode> visited) {
        ProtectedBy declared = findDeclaredProtection(mode);
        if (declared != null)
            out.add(declared);
        if (!visited.add(mode))
            return;
        if (mode instanceof MapperModePointer<?> pointer) {
            MapperMode pointed = pointer.point();
            if (pointed != null)
                collectProtections(pointed, out, visited);
        } else if (mode instanceof MapperModeCollection collection && collection.getConfiguredModes() != null) {
            for (MapperMode configured : collection.getConfiguredModes())
                collectProtections(configured, out, visited);
        }
    }

    private static ProtectedBy findDeclaredProtection(MapperMode mode) {
        Class<?> declaringClass = ((Enum<?>) mode).getDeclaringClass();
        try {
            ProtectedBy fieldLevel = declaringClass.getField(((Enum<?>) mode).name()).getAnnotation(ProtectedBy.class);
            if (fieldLevel != null)
                return fieldLevel;
        } catch (NoSuchFieldException ignored) {
        }
        return declaringClass.getAnnotation(ProtectedBy.class);
    }

    private void logProtectedModes() {
        if (MODE_PROTECTIONS.isEmpty())
            return;
        List<String> protectedModes = MODE_PROTECTIONS.entrySet().stream()
                .map(e -> e.getKey().getClass().getSimpleName() + "." + ((Enum<?>) e.getKey()).name()
                        + " -> " + e.getValue().stream()
                            .map(p -> Arrays.stream(p.value()).map(Permissions::name).toList() + (p.anyOf() ? " (anyOf)" : " (allOf)"))
                            .toList())
                .toList();
        log.info("Protected mapper modes: {}", StringUtils.join(protectedModes, ","));
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
        logProtectedModes();
    }
}
