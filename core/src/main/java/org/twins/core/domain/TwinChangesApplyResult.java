package org.twins.core.domain;


import org.apache.commons.collections4.IterableUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwinChangesApplyResult extends EntitiesChangesCollector {
    Map<Class<?>, Iterable<Object>> result = new HashMap<>();

    public TwinChangesApplyResult put(Class<?> clazz, Iterable<Object> entities) {
        result.put(clazz, entities);
        return this;
    }

    public <T> Iterable<T> getForClass(Class<T> clazz) {
        return (Iterable<T>) result.getOrDefault(clazz, Collections.emptyList() );
    }

    public <T> List<T> getForClassAsList(Class<T> clazz) {
        return IterableUtils.toList(getForClass(clazz));
    }
}
