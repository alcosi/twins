package org.cambium.common.kit;

import java.util.*;
import java.util.function.Function;

public class KitGrouped<E, K, GK> extends Kit<E, K>{
    protected Map<GK, List<E>> groupedMap;

    public static final KitGrouped EMPTY = new KitGrouped(null, e -> null, e -> null);

    private final Function<? super E, ? extends GK> functionGetGroupingId;

    public KitGrouped(Collection<E> collection, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId) {
        super(collection, functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    public KitGrouped(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId) {
        super(functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    @Override
    public boolean add(E e) {
        groupedMap = null; //invalidate
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> e) {
        groupedMap = null; //invalidate
        return super.addAll(e);
    }

    public Map<GK, List<E>> getGroupedMap() {
        if (groupedMap != null)
            return groupedMap;
        if (isEmpty() || functionGetGroupingId == null)
            return Collections.EMPTY_MAP;
        groupedMap = new HashMap<>();
        GK groupingId;
        for (E entity : collection) {
            groupingId = functionGetGroupingId.apply(entity);
            groupedMap.computeIfAbsent(groupingId, k -> new ArrayList<>());
            groupedMap.get(groupingId).add(entity);
        }
        return groupedMap;
    }

    public Set<GK> getGroupedKeySet() {
        getGroupedMap();
        if (groupedMap == null)
            return Collections.EMPTY_SET;
        return groupedMap.keySet();
    }

    public boolean containsGroupedKey(GK key) {
        getGroupedMap();
        if (groupedMap == null)
            return false;
        return groupedMap.containsKey(key);
    }

    public List<E> getGrouped(GK key) {
        getGroupedMap();
        if (groupedMap == null || groupedMap.isEmpty())
            return Collections.EMPTY_LIST;
        List<E> ret = groupedMap.get(key);
        return ret != null ? ret : Collections.EMPTY_LIST;
    }
}
