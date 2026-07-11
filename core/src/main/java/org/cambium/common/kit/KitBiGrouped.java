package org.cambium.common.kit;

import java.util.*;
import java.util.function.Function;

/**
 * Dual-grouping kit: same elements accessible by primary key {@link K},
 * by first grouping key {@link GK1} (inherited from {@link KitGrouped}),
 * and by second grouping key {@link GK2}.
 *
 * <p>Both grouping maps are lazy and invalidated on every mutation, mirroring
 * {@link KitGrouped#getGroupedMap()} semantics.
 */
public class KitBiGrouped<E, K, GK1, GK2> extends KitGrouped<E, K, GK1> {
    protected Map<GK2, List<E>> groupedMap2;

    private final Function<? super E, ? extends GK2> functionGetGroupingId2;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final KitBiGrouped EMPTY = new KitBiGrouped(
            Collections.emptyList(),
            e -> null,
            e -> null,
            e -> null
    ) {
        @Override public boolean add(Object e) { throw new UnsupportedOperationException("KitBiGrouped.EMPTY is immutable"); }
        @Override public boolean addAll(Collection c) { throw new UnsupportedOperationException("KitBiGrouped.EMPTY is immutable"); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException("KitBiGrouped.EMPTY is immutable"); }
        @Override public void clear() { throw new UnsupportedOperationException("KitBiGrouped.EMPTY is immutable"); }
    };

    public KitBiGrouped(Collection<E> collection,
                        Function<? super E, ? extends K> functionGetId,
                        Function<? super E, ? extends GK1> functionGetGroupingId,
                        Function<? super E, ? extends GK2> functionGetGroupingId2) {
        super(collection, functionGetId, functionGetGroupingId);
        this.functionGetGroupingId2 = functionGetGroupingId2;
    }

    public KitBiGrouped(Collection<E> collection,
                        Function<? super E, ? extends K> functionGetId,
                        Function<? super E, ? extends GK1> functionGetGroupingId,
                        Function<? super E, ? extends GK2> functionGetGroupingId2,
                        DuplicateKeyMode duplicateKeyMode) {
        super(collection, functionGetId, functionGetGroupingId, duplicateKeyMode);
        this.functionGetGroupingId2 = functionGetGroupingId2;
    }

    public KitBiGrouped(Function<? super E, ? extends K> functionGetId,
                        Function<? super E, ? extends GK1> functionGetGroupingId,
                        Function<? super E, ? extends GK2> functionGetGroupingId2) {
        super(functionGetId, functionGetGroupingId);
        this.functionGetGroupingId2 = functionGetGroupingId2;
    }

    public KitBiGrouped(Function<? super E, ? extends K> functionGetId,
                        Function<? super E, ? extends GK1> functionGetGroupingId,
                        Function<? super E, ? extends GK2> functionGetGroupingId2,
                        DuplicateKeyMode duplicateKeyMode) {
        super(functionGetId, functionGetGroupingId, duplicateKeyMode);
        this.functionGetGroupingId2 = functionGetGroupingId2;
    }

    @SuppressWarnings("unchecked")
    public static <E, K, GK1, GK2> KitBiGrouped<E, K, GK1, GK2> emptyKitBiGrouped() {
        return (KitBiGrouped<E, K, GK1, GK2>) EMPTY;
    }

    @Override
    public boolean add(E e) {
        groupedMap2 = null;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        groupedMap2 = null;
        return super.addAll(c);
    }

    @Override
    public boolean remove(Object o) {
        groupedMap2 = null;
        return super.remove(o);
    }

    @Override
    public void clear() {
        groupedMap2 = null;
        super.clear();
    }

    @Override
    public E removeByKey(K key) {
        groupedMap2 = null;
        return super.removeByKey(key);
    }

    public Map<GK2, List<E>> getGroupedMap2() {
        if (groupedMap2 != null)
            return groupedMap2;
        if (isEmpty() || functionGetGroupingId2 == null)
            return Collections.emptyMap();
        groupedMap2 = new HashMap<>();
        for (E entity : this) {
            GK2 groupingId2 = functionGetGroupingId2.apply(entity);
            groupedMap2.computeIfAbsent(groupingId2, k -> new ArrayList<>());
            groupedMap2.get(groupingId2).add(entity);
        }
        return groupedMap2;
    }

    public Set<GK2> getGroupedKeySet2() {
        getGroupedMap2();
        if (groupedMap2 == null)
            return Collections.emptySet();
        return groupedMap2.keySet();
    }

    public boolean containsGroupedKey2(GK2 key) {
        getGroupedMap2();
        if (groupedMap2 == null)
            return false;
        return groupedMap2.containsKey(key);
    }

    public List<E> getGrouped2(GK2 key) {
        getGroupedMap2();
        if (groupedMap2 == null || groupedMap2.isEmpty())
            return Collections.emptyList();
        List<E> ret = groupedMap2.get(key);
        return ret != null ? ret : Collections.emptyList();
    }
}
