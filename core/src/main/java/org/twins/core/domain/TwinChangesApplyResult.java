package org.twins.core.domain;


import org.apache.commons.collections4.IterableUtils;

import java.util.*;
import java.util.function.Function;

public class TwinChangesApplyResult {
    Map<Class<?>, Iterable<Object>> result = new HashMap<>();

    public TwinChangesApplyResult put(Class<?> clazz, Iterable<Object> entities) {
        result.put(clazz, entities);
        return this;
    }

    public <T> Iterable<T> getForClass(Class<T> clazz) {
        return (Iterable<T>) result.getOrDefault(clazz, Collections.emptyList() );
    }

    public <T> T getById(Class<T> clazz, Function<T, UUID> functionGetId, UUID id) {
        Collection<T> items = (Collection<T>) result.getOrDefault(clazz, Collections.emptyList());

        return items.stream()
                .filter(item -> Objects.equals(functionGetId.apply(item), id))
                .findFirst()
                .orElse(null);
    }

    public <T> List<T> getForClassAsList(Class<T> clazz) {
        return IterableUtils.toList(getForClass(clazz));
    }
}
