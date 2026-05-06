package org.cambium.common.kit;

import java.util.*;
import java.util.function.Function;

public class KitGrouped<E, K, GK> extends Kit<E, K> {
    protected Map<GK, List<E>> groupedMap;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final KitGrouped EMPTY = new KitGrouped(Collections.emptyList(), e -> null, e -> null) {
        @Override public boolean add(Object e) { throw new UnsupportedOperationException("KitGrouped.EMPTY is immutable"); }
        @Override public boolean addAll(Collection c) { throw new UnsupportedOperationException("KitGrouped.EMPTY is immutable"); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException("KitGrouped.EMPTY is immutable"); }
        @Override public boolean removeAll(Collection c) { throw new UnsupportedOperationException("KitGrouped.EMPTY is immutable"); }
        @Override public boolean retainAll(Collection c) { throw new UnsupportedOperationException("KitGrouped.EMPTY is immutable"); }
        @Override public void clear() { throw new UnsupportedOperationException("KitGrouped.EMPTY is immutable"); }
    };

    private final Function<? super E, ? extends GK> functionGetGroupingId;

    public KitGrouped(Collection<E> collection, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId) {
        super(collection, functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    public KitGrouped(Collection<E> collection, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, DuplicateKeyMode duplicateKeyMode) {
        super(collection, functionGetId, duplicateKeyMode);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    public KitGrouped(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId) {
        super(functionGetId);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    public KitGrouped(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, DuplicateKeyMode duplicateKeyMode) {
        super(functionGetId, duplicateKeyMode);
        this.functionGetGroupingId = functionGetGroupingId;
    }

    @Override
    public boolean add(E e) {
        groupedMap = null;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        groupedMap = null;
        return super.addAll(c);
    }

    public Map<GK, List<E>> getGroupedMap() {
        if (groupedMap != null)
            return groupedMap;
        if (isEmpty() || functionGetGroupingId == null)
            return Collections.emptyMap();
        groupedMap = new HashMap<>();
        for (E entity : collection) {
            GK groupingId = functionGetGroupingId.apply(entity);
            groupedMap.computeIfAbsent(groupingId, k -> new ArrayList<>());
            groupedMap.get(groupingId).add(entity);
        }
        return groupedMap;
    }

    public Set<GK> getGroupedKeySet() {
        getGroupedMap();
        if (groupedMap == null)
            return Collections.emptySet();
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
            return Collections.emptyList();
        List<E> ret = groupedMap.get(key);
        return ret != null ? ret : Collections.emptyList();
    }
}
