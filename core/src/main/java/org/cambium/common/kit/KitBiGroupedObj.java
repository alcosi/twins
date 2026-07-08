package org.cambium.common.kit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Dual-grouping kit with representative objects per group: same elements accessible
 * by primary key {@link K}, by two grouping keys {@link GK1}/{@link GK2} (inherited
 * from {@link KitBiGrouped}), and each group additionally mapped to a representative
 * object ({@link GE1} for {@link GK1} groups, {@link GE2} for {@link GK2} groups).
 *
 * <p>Representative object is extracted from the first element of each group, mirroring
 * {@link KitGroupedObj} semantics.
 */
public class KitBiGroupedObj<E, K, GK1, GK2, GE1, GE2> extends KitBiGrouped<E, K, GK1, GK2> {
    protected Map<GK1, GE1> groupingObject1Map;
    protected Map<GK2, GE2> groupingObject2Map;

    private final Function<? super E, ? extends GE1> functionGetGroupingObject1;
    private final Function<? super E, ? extends GE2> functionGetGroupingObject2;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final KitBiGroupedObj EMPTY = new KitBiGroupedObj(
            Collections.emptyList(),
            e -> null,
            e -> null,
            e -> null,
            e -> null,
            e -> null
    ) {
        @Override public boolean add(Object e) { throw new UnsupportedOperationException("KitBiGroupedObj.EMPTY is immutable"); }
        @Override public boolean addAll(Collection c) { throw new UnsupportedOperationException("KitBiGroupedObj.EMPTY is immutable"); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException("KitBiGroupedObj.EMPTY is immutable"); }
        @Override public Object removeByKey(Object key) { throw new UnsupportedOperationException("KitBiGroupedObj.EMPTY is immutable"); }
        @Override public void clear() { throw new UnsupportedOperationException("KitBiGroupedObj.EMPTY is immutable"); }
        @Override public KitBiGroupedObj freeze() { return this; }
    };

    @SuppressWarnings("unchecked")
    public static <E, K, GK1, GK2, GE1, GE2> KitBiGroupedObj<E, K, GK1, GK2, GE1, GE2> emptyKitBiGroupedObj() {
        return (KitBiGroupedObj<E, K, GK1, GK2, GE1, GE2>) EMPTY;
    }

    public KitBiGroupedObj(Collection<E> collection,
                           Function<? super E, ? extends K> functionGetId,
                           Function<? super E, ? extends GK1> functionGetGroupingId,
                           Function<? super E, ? extends GK2> functionGetGroupingId2,
                           Function<? super E, ? extends GE1> functionGetGroupingObject1,
                           Function<? super E, ? extends GE2> functionGetGroupingObject2) {
        super(collection, functionGetId, functionGetGroupingId, functionGetGroupingId2);
        this.functionGetGroupingObject1 = functionGetGroupingObject1;
        this.functionGetGroupingObject2 = functionGetGroupingObject2;
    }

    public KitBiGroupedObj(Collection<E> collection,
                           Function<? super E, ? extends K> functionGetId,
                           Function<? super E, ? extends GK1> functionGetGroupingId,
                           Function<? super E, ? extends GK2> functionGetGroupingId2,
                           Function<? super E, ? extends GE1> functionGetGroupingObject1,
                           Function<? super E, ? extends GE2> functionGetGroupingObject2,
                           DuplicateKeyMode duplicateKeyMode) {
        super(collection, functionGetId, functionGetGroupingId, functionGetGroupingId2, duplicateKeyMode);
        this.functionGetGroupingObject1 = functionGetGroupingObject1;
        this.functionGetGroupingObject2 = functionGetGroupingObject2;
    }

    public KitBiGroupedObj(Function<? super E, ? extends K> functionGetId,
                           Function<? super E, ? extends GK1> functionGetGroupingId,
                           Function<? super E, ? extends GK2> functionGetGroupingId2,
                           Function<? super E, ? extends GE1> functionGetGroupingObject1,
                           Function<? super E, ? extends GE2> functionGetGroupingObject2) {
        super(functionGetId, functionGetGroupingId, functionGetGroupingId2);
        this.functionGetGroupingObject1 = functionGetGroupingObject1;
        this.functionGetGroupingObject2 = functionGetGroupingObject2;
    }

    public KitBiGroupedObj(Function<? super E, ? extends K> functionGetId,
                           Function<? super E, ? extends GK1> functionGetGroupingId,
                           Function<? super E, ? extends GK2> functionGetGroupingId2,
                           Function<? super E, ? extends GE1> functionGetGroupingObject1,
                           Function<? super E, ? extends GE2> functionGetGroupingObject2,
                           DuplicateKeyMode duplicateKeyMode) {
        super(functionGetId, functionGetGroupingId, functionGetGroupingId2, duplicateKeyMode);
        this.functionGetGroupingObject1 = functionGetGroupingObject1;
        this.functionGetGroupingObject2 = functionGetGroupingObject2;
    }

    @Override
    public boolean add(E e) {
        groupingObject1Map = null;
        groupingObject2Map = null;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        groupingObject1Map = null;
        groupingObject2Map = null;
        return super.addAll(c);
    }

    @Override
    public boolean remove(Object o) {
        groupingObject1Map = null;
        groupingObject2Map = null;
        return super.remove(o);
    }

    @Override
    public void clear() {
        groupingObject1Map = null;
        groupingObject2Map = null;
        super.clear();
    }

    public Map<GK1, GE1> getGroupingObject1Map() {
        if (groupingObject1Map != null)
            return groupingObject1Map;
        if (isEmpty() || functionGetGroupingObject1 == null)
            return Collections.emptyMap();
        getGroupedMap();
        if (groupedMap == null)
            return Collections.emptyMap();
        groupingObject1Map = new HashMap<>();
        for (Map.Entry<GK1, List<E>> entry : groupedMap.entrySet()) {
            GK1 groupingId = entry.getKey();
            if (CollectionUtils.isEmpty(entry.getValue()))
                continue;
            GE1 groupingObject = functionGetGroupingObject1.apply(entry.getValue().get(0));
            groupingObject1Map.put(groupingId, groupingObject);
        }
        return groupingObject1Map;
    }

    public Map<GK2, GE2> getGroupingObject2Map() {
        if (groupingObject2Map != null)
            return groupingObject2Map;
        if (isEmpty() || functionGetGroupingObject2 == null)
            return Collections.emptyMap();
        getGroupedMap2();
        if (groupedMap2 == null)
            return Collections.emptyMap();
        groupingObject2Map = new HashMap<>();
        for (Map.Entry<GK2, List<E>> entry : groupedMap2.entrySet()) {
            GK2 groupingId = entry.getKey();
            if (CollectionUtils.isEmpty(entry.getValue()))
                continue;
            GE2 groupingObject = functionGetGroupingObject2.apply(entry.getValue().get(0));
            groupingObject2Map.put(groupingId, groupingObject);
        }
        return groupingObject2Map;
    }

    public List<ImmutablePair<GE1, List<E>>> getGroupedList1() {
        var ret = new ArrayList<ImmutablePair<GE1, List<E>>>();
        for (var entry : getGroupedMap().entrySet()) {
            ret.add(new ImmutablePair<>(getGroupingObject1(entry.getKey()), entry.getValue()));
        }
        return ret;
    }

    public List<ImmutablePair<GE2, List<E>>> getGroupedList2() {
        var ret = new ArrayList<ImmutablePair<GE2, List<E>>>();
        for (var entry : getGroupedMap2().entrySet()) {
            ret.add(new ImmutablePair<>(getGroupingObject2(entry.getKey()), entry.getValue()));
        }
        return ret;
    }

    public GE1 getGroupingObject1(GK1 key) {
        getGroupingObject1Map();
        if (MapUtils.isEmpty(groupingObject1Map))
            return null;
        return groupingObject1Map.get(key);
    }

    public GE2 getGroupingObject2(GK2 key) {
        getGroupingObject2Map();
        if (MapUtils.isEmpty(groupingObject2Map))
            return null;
        return groupingObject2Map.get(key);
    }
}
