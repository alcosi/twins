package org.cambium.common.kit;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Dual-access container: implements Collection&lt;E&gt; for sequential access
 * and provides Map&lt;K, E&gt; for key-based lookup via functionGetId.
 * <p>
 * Not thread-safe. Do not share between threads.
 * <p>
 * Duplicate key handling depends on {@link DuplicateKeyMode}:
 * <ul>
 *   <li>SILENT (default) — keeps the first element, silently discards duplicates</li>
 *   <li>REPLACE — replaces with the newer element</li>
 *   <li>THROW — throws ServiceException if a different object with the same key is added</li>
 * </ul>
 * Duplicate detection uses a three-level check: identity (==) → equals() → action.
 */
public class Kit<E, K> implements Collection<E> {
    protected Collection<E> collection;
    protected Map<K, E> map;
    protected TreeMap<String, E> caseInsensitiveMap;
    protected final Function<? super E, ? extends K> functionGetId;
    protected final DuplicateKeyMode duplicateKeyMode;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Kit EMPTY = new Kit(Collections.emptyList(), e -> null) {
        @Override public boolean add(Object e) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public boolean addAll(Collection c) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public boolean remove(Object o) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public boolean removeAll(Collection c) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public boolean retainAll(Collection c) { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
        @Override public void clear() { throw new UnsupportedOperationException("Kit.EMPTY is immutable"); }
    };

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetId) {
        this(collection, functionGetId, DuplicateKeyMode.SILENT);
    }

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetId, DuplicateKeyMode duplicateKeyMode) {
        this.collection = collection;
        this.functionGetId = functionGetId;
        this.duplicateKeyMode = duplicateKeyMode != null ? duplicateKeyMode : DuplicateKeyMode.SILENT;
        if (this.duplicateKeyMode == DuplicateKeyMode.THROW && collection != null && !collection.isEmpty()) {
            getMap();
        }
    }

    public Kit(Function<? super E, ? extends K> functionGetId) {
        this(functionGetId, DuplicateKeyMode.SILENT);
    }

    public Kit(Function<? super E, ? extends K> functionGetId, DuplicateKeyMode duplicateKeyMode) {
        this.functionGetId = functionGetId;
        this.duplicateKeyMode = duplicateKeyMode != null ? duplicateKeyMode : DuplicateKeyMode.SILENT;
    }

    /**
     * Type-safe alternative to Kit.EMPTY. Returns the shared immutable empty Kit instance.
     */
    @SuppressWarnings("unchecked")
    public static <E, K> Kit<E, K> emptyKit() {
        return (Kit<E, K>) EMPTY;
    }

    public Collection<E> getCollection() {
        if (collection != null)
            return collection;
        return Collections.emptyList();
    }

    public List<E> getList() {
        if (collection == null)
            return Collections.emptyList();
        if (collection instanceof List)
            return (List<E>) collection;
        else
            return new ArrayList<>(collection);
    }

    public boolean add(E e) {
        if (collection == null)
            collection = new ArrayList<>();
        K key = functionGetId.apply(e);
        if (map == null && !isEmpty())
            getMap();
        if (map != null) {
            E existing = map.get(key);
            if (existing != null) {
                if (existing == e)
                    return false;
                switch (duplicateKeyMode) {
                    case REPLACE -> { map.put(key, e); caseInsensitiveMap = null; }
                    case THROW -> {
                        if (!existing.equals(e))
                            throw new IllegalStateException(
                                    "Kit already contains entry with key " + key
                                            + ", existing: " + existing + ", new: " + e);
                        return false;
                    }
                    default -> {}
                }
            } else {
                map.put(key, e);
                caseInsensitiveMap = null;
            }
        }
        return collection.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        if (collection == null)
            collection = new ArrayList<>();
        boolean modified = false;
        for (E e : c) {
            if (add(e))
                modified = true;
        }
        return modified;
    }

    public Map<K, E> getMap() {
        if (map != null)
            return map;
        if (collection == null)
            return Collections.emptyMap();
        int size = collection.size();
        map = new LinkedHashMap<>(size * 4 / 3 + 1);
        for (E e : collection) {
            K key = functionGetId.apply(e);
            E existing = map.get(key);
            if (existing == null) {
                map.put(key, e);
            } else {
                switch (duplicateKeyMode) {
                    case REPLACE -> map.put(key, e);
                    case THROW -> {
                        if (existing != e && !existing.equals(e))
                            throw new IllegalStateException(
                                    "Kit already contains entry with key " + key
                                            + ", existing: " + existing + ", new: " + e);
                    }
                    default -> {}
                }
            }
        }
        return map;
    }

    public boolean containsKey(K key) {
        getMap();
        if (map == null)
            return false;
        return map.containsKey(key);
    }

    public boolean containsKeyIgnoreCase(K key) {
        getMap();
        if (map == null || key == null)
            return false;
        if (!(key instanceof String))
            return map.containsKey(key);
        String searchKey = (String) key;
        if (caseInsensitiveMap == null) {
            caseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<K, E> entry : map.entrySet()) {
                if (entry.getKey() instanceof String)
                    caseInsensitiveMap.put((String) entry.getKey(), entry.getValue());
            }
        }
        return caseInsensitiveMap.containsKey(searchKey);
    }

    public E get(K key) {
        getMap();
        if (map == null)
            return null;
        return map.get(key);
    }

    public E getSafe(K key) throws ServiceException {
        E entry = get(key);
        if (entry == null)
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Kit does not contain entry with key {}", key);
        return entry;
    }

    public Set<K> getIdSet() {
        if (map == null)
            getMap();
        if (map == null)
            return Collections.emptySet();
        return map.keySet();
    }

    public Set<K> getIdSetSafe() {
        return getIdSet();
    }

    @Override
    public String toString() {
        return "size = " + (collection != null ? CollectionUtils.size(collection) : "0");
    }

    //collection stuff

    @Override
    public boolean remove(Object o) {
        if (collection == null)
            return false;
        boolean ret = collection.remove(o);
        if (ret && map != null) {
            try {
                @SuppressWarnings("unchecked")
                K key = functionGetId.apply((E) o);
                map.remove(key);
                caseInsensitiveMap = null;
            } catch (ClassCastException ignored) {
            }
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (collection == null)
            return c.isEmpty();
        return collection.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (collection == null)
            return false;
        boolean ret = collection.removeAll(c);
        if (ret) {
            if (map != null) {
                for (Object o : c) {
                    try {
                        @SuppressWarnings("unchecked")
                        K key = functionGetId.apply((E) o);
                        map.remove(key);
                    } catch (ClassCastException ignored) {
                    }
                }
                caseInsensitiveMap = null;
            }
        }
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (collection == null)
            return false;
        map = null;
        caseInsensitiveMap = null;
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        map = null;
        caseInsensitiveMap = null;
        if (collection != null)
            collection.clear();
    }

    @Override
    public int size() {
        if (collection == null)
            return 0;
        return CollectionUtils.size(collection);
    }

    public boolean isEmpty() {
        return collection == null || collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection != null && collection.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new KitIterator();
    }

    @Override
    public Object[] toArray() {
        return collection != null ? collection.toArray() : new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (collection != null)
            return collection.toArray(a);
        if (a.length > 0)
            a[0] = null;
        return a;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public DuplicateKeyMode getDuplicateKeyMode() {
        return duplicateKeyMode;
    }

    private class KitIterator implements Iterator<E> {
        Iterator<E> collectionIterator = collection != null ? collection.iterator() : Collections.emptyIterator();
        E currentItem;

        @Override
        public boolean hasNext() {
            return collectionIterator.hasNext();
        }

        @Override
        public E next() {
            currentItem = collectionIterator.next();
            return currentItem;
        }

        @Override
        public void remove() {
            collectionIterator.remove();
            if (map != null && currentItem != null)
                map.remove(functionGetId.apply(currentItem));
            caseInsensitiveMap = null;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            collectionIterator.forEachRemaining(action);
        }
    }
}
