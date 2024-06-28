package org.cambium.common.kit;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Kit<E, K> {
    @Getter
    protected Collection<E> collection;
    protected Map<K, E> map;
    protected final Function<? super E, ? extends K> functionGetId;

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetId) {
        this.collection = collection;
        this.functionGetId = functionGetId;
    }

    public Kit(Function<? super E, ? extends K> functionGetId) {
        this.functionGetId = functionGetId;
    }

    public Collection<E> getCollection() {
        if (collection != null)
            return collection;
        return Collections.EMPTY_LIST;
    }

    public Kit<E, K> add(E e) {
        if (collection == null)
            collection = new ArrayList<>();
        collection.add(e);
        map = null; //invalidate
        return this;
    }

    public Kit<E, K> addAll(Collection<E> e) {
        if (collection == null)
            collection = new ArrayList<>();
        collection.addAll(e);
        map = null; //invalidate
        return this;
    }

    public Map<K, E> getMap() {
        if (map != null)
            return map;
        if (collection == null)
            return Collections.EMPTY_MAP;
        map = collection
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
        return collection == null || collection.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
