package org.cambium.common;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Kit<E> {
    @Getter
    private final List<E> list;
    private Map<UUID, E> map;
    private final Function<? super E, ? extends UUID> functionGetId;

    public Kit(List<E> list, Function<? super E, ? extends UUID> functionGetId) {
        this.list = list;
        this.functionGetId = functionGetId;
    }

    public Map<UUID, E> getMap() {
        if (map != null)
            return map;
        if (list == null)
            return null;
        map = list
                .stream().collect(Collectors.toMap(functionGetId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        return map;
    }

    public Set<UUID> getIdSet() {
        if (map == null)
            getMap();
        if (map == null)
            return null;
        return map.keySet();
    }
}
