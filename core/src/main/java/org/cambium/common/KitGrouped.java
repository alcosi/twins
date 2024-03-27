package org.cambium.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class KitGrouped<E> extends Kit<E>{

    private Map<UUID, List<E>> groupedMap;

    private final Function<? super E, ? extends UUID> functionGetGroupingId;

    public KitGrouped(List<E> list, Function<? super E, ? extends UUID> functionGetId, Function<? super E, ? extends UUID> functionGetGroupingId) {
        super(functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    public KitGrouped(Function<? super E, ? extends UUID> functionGetId, Function<? super E, ? extends UUID> functionGetGroupingId) {
        super(functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }


    public Map<UUID, List<E>> getGroupedMap() {
        if (groupedMap != null)
            return groupedMap;
        if (list == null || functionGetGroupingId == null)
            return null;
        UUID groupingId;
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
        if (groupedMap == null)
            return null;
        return groupedMap.get(key);
    }

}
