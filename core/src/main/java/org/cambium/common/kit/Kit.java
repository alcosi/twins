package org.cambium.common.kit;

import lombok.Getter;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;

import java.util.*;
import java.util.function.Function;

/**
 * Dual-access container: implements Iterable&lt;E&gt; for sequential access
 * and provides Map&lt;K, E&gt; for key-based lookup via functionGetKey.
 * <p>
 * Not thread-safe. Do not share between threads.
 * <p>
 * Duplicate key handling depends on {@link DuplicateKeyMode}:
 * <ul>
 *   <li>IGNORE (default) — keeps the first element, silently discards duplicates</li>
 *   <li>REPLACE — replaces with the newer element</li>
 *   <li>THROW — throws IllegalStateException if a different object with the same key is added</li>
 * </ul>
 * Duplicate detection uses a three-level check: identity (==) → equals() → action.
 */
public class Kit<E, K> implements Iterable<E> {
    protected LinkedHashMap<K, E> map;
    protected final Function<? super E, ? extends K> functionGetKey;
    protected final Function<? super K, ? extends K> functionFormatKey;
    @Getter
    protected final DuplicateKeyMode duplicateKeyMode;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Kit EMPTY = new Kit(Collections.emptyList(), e -> null) {
        @Override public boolean add(Object e) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public boolean addAll(Collection c) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public void clear() { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
    };

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetKey) {
        this(collection, functionGetKey, DuplicateKeyMode.THROW);
    }

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetKey, DuplicateKeyMode duplicateKeyMode) {
        this(collection, functionGetKey, null, duplicateKeyMode);
    }

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetKey, Function<? super K, ? extends K> functionFormatKey, DuplicateKeyMode duplicateKeyMode) {
        this.functionGetKey = functionGetKey;
        this.functionFormatKey = functionFormatKey;
        this.duplicateKeyMode = duplicateKeyMode != null ? duplicateKeyMode : DuplicateKeyMode.THROW;
        if (collection != null && !collection.isEmpty()) {
            map = new LinkedHashMap<>(collection.size() * 4 / 3 + 1);
            for (E e : collection)
                add(e);
        }
    }

    public Kit(Function<? super E, ? extends K> functionGetKey) {
        this(null, functionGetKey, DuplicateKeyMode.IGNORE);
    }

    public Kit(Function<? super E, ? extends K> functionGetKey, Function<? super K, ? extends K> functionFormatKey) {
        this(null, functionGetKey, functionFormatKey, DuplicateKeyMode.IGNORE);
    }

    public Kit(Function<? super E, ? extends K> functionGetKey, DuplicateKeyMode duplicateKeyMode) {
        this(null, functionGetKey, duplicateKeyMode);
    }

    @SuppressWarnings("unchecked")
    public static <E, K> Kit<E, K> emptyKit() {
        return (Kit<E, K>) EMPTY;
    }

    public Collection<E> getCollection() {
        if (map == null || map.isEmpty())
            return Collections.emptyList();
        return Collections.unmodifiableCollection(map.values());
    }

    public List<E> getList() {
        if (map == null || map.isEmpty())
            return Collections.emptyList();
        return new ArrayList<>(map.values());
    }

    public boolean add(E e) {
        if (map == null)
            map = new LinkedHashMap<>();
        K key = functionGetKey.apply(e);
        if (functionFormatKey != null) {
            key = functionFormatKey.apply(key);
        }
        E existing = map.get(key);
        if (existing != null) {
            if (existing == e)
                return false;
            switch (duplicateKeyMode) {
                case REPLACE -> {
                    map.put(key, e);
                    return true;
                }
                case THROW -> {
                    if (!existing.equals(e))
                        throw new IllegalStateException(
                            "Kit already contains entry with key " + key
                                + ", existing: " + existing + ", new: " + e);
                    return false;
                }
                default -> { return false; } // IGNORE — keep first, don't add
            }
        }
        map.put(key, e);
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            if (add(e))
                modified = true;
        }
        return modified;
    }

    public Map<K, E> getMap() {
        if (map == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(map);
    }

    protected K formatKey(K key) {
        return functionFormatKey != null ? functionFormatKey.apply(key) : key;
    }

    public boolean containsKey(K key) {
        if (map == null)
            return false;
        return map.containsKey(formatKey(key));
    }

    public E get(K key) {
        if (map == null)
            return null;
        return map.get(formatKey(key));
    }

    public E getSafe(K key) throws ServiceException {
        E entry = get(key);
        if (entry == null)
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Kit does not contain entry with key {}", key);
        return entry;
    }

    public Set<K> getIdSet() {
        if (map == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(map.keySet());
    }

    public Set<K> getIdSetSafe() {
        return getIdSet();
    }

    @Override
    public String toString() {
        return "size = " + (map != null ? map.size() : "0");
    }

    public E removeByKey(K key) {
        if (map == null)
            return null;
        return map.remove(formatKey(key));
    }

    public boolean remove(Object o) {
        if (map == null)
            return false;
        try {
            @SuppressWarnings("unchecked")
            K key = functionGetKey.apply((E) o);
            return map.remove(formatKey(key)) != null;
        } catch (ClassCastException ignored) {
            return false;
        }
    }

    public boolean contains(Object o) {
        return map != null && map.containsValue(o);
    }

    public void clear() {
        if (map != null)
            map.clear();
    }

    public int size() {
        return map == null ? 0 : map.size();
    }

    public boolean isEmpty() {
        return map == null || map.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return map != null ? map.values().iterator() : Collections.emptyIterator();
    }

    public Object[] toArray() {
        return map != null ? map.values().toArray() : new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        if (map != null)
            return map.values().toArray(a);
        if (a.length > 0)
            a[0] = null;
        return a;
    }

}
