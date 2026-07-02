package org.cambium.common.kit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;

import java.util.*;
import java.util.function.Function;

public class KitGroupedObj<E, K, GK, GE> extends KitGrouped<E, K, GK> {
    protected Map<GK, GE> groupingObjectMap;
    private final Function<? super E, ? extends GE> functionGetGroupingObject;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final KitGroupedObj EMPTY = new KitGroupedObj(
            Collections.emptyList(),
            e -> null,
            e -> null,
            e -> null
    ) {
        @Override public boolean add(Object e) { throw new UnsupportedOperationException("KitGroupedObj.EMPTY is immutable"); }
        @Override public boolean addAll(Collection c) { throw new UnsupportedOperationException("KitGroupedObj.EMPTY is immutable"); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException("KitGroupedObj.EMPTY is immutable"); }
        @Override public Object removeByKey(Object key) { throw new UnsupportedOperationException("KitGroupedObj.EMPTY is immutable"); }
        @Override public void clear() { throw new UnsupportedOperationException("KitGroupedObj.EMPTY is immutable"); }
        @Override public KitGroupedObj freeze() { return this; }
    };

    @SuppressWarnings("unchecked")
    public static <E, K, GK, GE> KitGroupedObj<E, K, GK, GE> emptyKitGroupedObj() {
        return (KitGroupedObj<E, K, GK, GE>) EMPTY;
    }

    public KitGroupedObj(Collection<E> collection, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, Function<? super E, ? extends GE> functionGetGroupingObject) {
        super(collection, functionGetId, functionGetGroupingId);
        this.functionGetGroupingObject = functionGetGroupingObject;
    }

    public KitGroupedObj(Collection<E> collection, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, Function<? super E, ? extends GE> functionGetGroupingObject, DuplicateKeyMode duplicateKeyMode) {
        super(collection, functionGetId, functionGetGroupingId, duplicateKeyMode);
        this.functionGetGroupingObject = functionGetGroupingObject;
    }

    public KitGroupedObj(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, Function<? super E, ? extends GE> functionGetGroupingObject) {
        super(functionGetId, functionGetGroupingId);
        this.functionGetGroupingObject = functionGetGroupingObject;
    }

    public KitGroupedObj(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, Function<? super E, ? extends GE> functionGetGroupingObject, DuplicateKeyMode duplicateKeyMode) {
        super(functionGetId, functionGetGroupingId, duplicateKeyMode);
        this.functionGetGroupingObject = functionGetGroupingObject;
    }

    @Override
    public boolean add(E e) {
        groupingObjectMap = null;
        return super.add(e);
    }

    public Map<GK, GE> getGroupingObjectMap() {
        if (groupingObjectMap != null)
            return groupingObjectMap;
        if (isEmpty() || functionGetGroupingObject == null)
            return Collections.emptyMap();
        getGroupedMap();
        if (groupedMap == null)
            return Collections.emptyMap();
        groupingObjectMap = new HashMap<>();
        for (Map.Entry<GK, List<E>> entry : groupedMap.entrySet()) {
            GK groupingId = entry.getKey();
            if (CollectionUtils.isEmpty(entry.getValue()))
                continue;
            GE groupingObject = functionGetGroupingObject.apply(entry.getValue().get(0));
            groupingObjectMap.put(groupingId, groupingObject);
        }
        return groupingObjectMap;
    }

    public List<ImmutablePair<GE, List<E>>> getGroupedList() {
        var ret = new ArrayList<ImmutablePair<GE, List<E>>>();
        for (var entry : getGroupedMap().entrySet()) {
            ret.add(new ImmutablePair<>(getGroupingObject(entry.getKey()), entry.getValue()));
        }
        return ret;
    }

    public GE getGroupingObject(GK key) {
        getGroupingObjectMap();
        if (MapUtils.isEmpty(groupingObjectMap))
            return null;
        return groupingObjectMap.get(key);
    }
}
