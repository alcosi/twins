package org.cambium.common;

import java.util.*;
import java.util.function.Function;

public class KitGrouped<E, K, GK> extends Kit<E, K>{

    private Map<GK, List<E>> groupedMap;

    private final Function<? super E, ? extends GK> functionGetGroupingId;

    public KitGrouped(List<E> list, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId) {
        super(list, functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    public KitGrouped(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId) {
        super(functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    @Override
    public Kit<E, K> add(E e) {
        groupedMap = null; //invalidate
        return super.add(e);
    }

    public Map<GK, List<E>> getGroupedMap() {
        if (groupedMap != null)
            return groupedMap;
        if (isEmpty() || functionGetGroupingId == null)
            return Collections.EMPTY_MAP;
        groupedMap = new HashMap<>();
        GK groupingId;
        for (E entity : list) {
            groupingId = functionGetGroupingId.apply(entity);
            groupedMap.computeIfAbsent(groupingId, k -> new ArrayList<>());
            groupedMap.get(groupingId).add(entity);
        }
        return groupedMap;
    }

    public boolean containsGroupedKey(UUID key) {
        getGroupedMap();
        if (groupedMap == null)
            return false;
        return groupedMap.containsKey(key);
    }

    public List<E> getGrouped(UUID key) {
        getGroupedMap();
        if (groupedMap == null || groupedMap.isEmpty())
            return Collections.EMPTY_LIST;
        List<E> ret = groupedMap.get(key);
        return ret != null ? ret : Collections.EMPTY_LIST;
    }

}
