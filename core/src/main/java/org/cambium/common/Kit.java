package org.cambium.common;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Kit<E, K> {
    @Getter
    protected List<E> list;
    protected Map<K, E> map;
    protected final Function<? super E, ? extends K> functionGetId;

    public Kit(List<E> list, Function<? super E, ? extends K> functionGetId) {
        this.list = list;
        this.functionGetId = functionGetId;
    }

    public Kit(Function<? super E, ? extends K> functionGetId) {
        this.functionGetId = functionGetId;
    }

    public List<E> getList() {
        if (list != null)
            return list;
        return Collections.EMPTY_LIST;
    }

    public Kit<E, K> add(E e) {
        if (list == null)
            list = new ArrayList<>();
        list.add(e);
        map = null; //invalidate
        return this;
    }

    public Map<K, E> getMap() {
        if (map != null)
            return map;
        if (list == null)
            return Collections.EMPTY_MAP;
        map = list
                .stream().collect(Collectors.toMap(functionGetId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        return map;
    }

    public boolean containsKey(K key) {
        getMap();
        if (map == null)
            return false;
        return map.containsKey(key);
    }

    public E get(K key) {
        getMap();
        if (map == null)
            return null;
        return map.get(key);
    }

    public Set<K> getIdSet() {
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
