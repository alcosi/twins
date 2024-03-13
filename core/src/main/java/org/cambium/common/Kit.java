package org.cambium.common;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Kit<E> {
    @Getter
    private List<E> list;
    private Map<UUID, E> map;
    private final Function<? super E, ? extends UUID> functionGetId;

    public Kit(List<E> list, Function<? super E, ? extends UUID> functionGetId) {
        this.list = list;
        this.functionGetId = functionGetId;
    }

    public Kit(Function<? super E, ? extends UUID> functionGetId) {
        this.functionGetId = functionGetId;
    }

    public Kit<E> add(E e) {
        if (list == null)
            list = new ArrayList<>();
        list.add(e);
        map = null; //invalidate
        return this;
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

    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
