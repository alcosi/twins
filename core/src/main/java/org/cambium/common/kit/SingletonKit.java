package org.cambium.common.kit;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;

import java.util.*;
import java.util.function.Function;
/**
 * Immutable singleton implementation of Kit with minimal allocations and cached views.
 */
public final class SingletonKit<E, K> extends Kit<E, K> {

    private final E element;
    private final K key;

    // Cached immutable views (created once)
    private final List<E> singletonList;
    private final Map<K, E> singletonMap;
    private final Set<K> singletonSet;

    public SingletonKit(E element,
                        Function<? super E, ? extends K> functionGetKey) {
        this(element, functionGetKey, null, DuplicateKeyMode.IGNORE);
    }

    public SingletonKit(E element,
                        Function<? super E, ? extends K> functionGetKey,
                        Function<? super K, ? extends K> functionFormatKey) {
        this(element, functionGetKey, functionFormatKey, DuplicateKeyMode.IGNORE);
    }

    public SingletonKit(E element,
                        Function<? super E, ? extends K> functionGetKey,
                        Function<? super K, ? extends K> functionFormatKey,
                        DuplicateKeyMode duplicateKeyMode) {

        super(null, functionGetKey, functionFormatKey,
                duplicateKeyMode != null ? duplicateKeyMode : DuplicateKeyMode.IGNORE);

        if (element == null) {
            throw new NullPointerException("SingletonKit does not support null element");
        }

        K computedKey = functionGetKey.apply(element);
        if (computedKey == null) {
            throw new NullPointerException("Kit does not support null keys. Element: " + element);
        }

        this.element = element;
        this.key = formatKey(computedKey);

        // Кэшируем singleton-коллекции один раз при создании
        this.singletonList = Collections.singletonList(element);
        this.singletonMap = Collections.singletonMap(key, element);
        this.singletonSet = Collections.singleton(key);
    }

    // ====================== Immutable operations ======================

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("SingletonKit is immutable");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("SingletonKit is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("SingletonKit is immutable");
    }

    @Override
    public E removeByKey(K key) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    // ====================== Fast accessors ======================

    @Override
    public Collection<E> getCollection() {
        return singletonList;
    }

    @Override
    public List<E> getList() {
        return singletonList;
    }

    @Override
    public Map<K, E> getMap() {
        return singletonMap;
    }

    @Override
    public Set<K> getIdSet() {
        return singletonSet;
    }

    @Override
    public boolean containsKey(K lookupKey) {
        return Objects.equals(key, formatKey(lookupKey));
    }

    @Override
    public E get(K lookupKey) {
        return Objects.equals(key, formatKey(lookupKey)) ? element : null;
    }

    @Override
    public E getSafe(K lookupKey) throws ServiceException {
        if (Objects.equals(key, formatKey(lookupKey))) {
            return element;
        }
        throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN,
                "Kit does not contain entry with key {}", lookupKey);
    }

    @Override
    public boolean contains(Object o) {
        return Objects.equals(element, o);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isNotEmpty() {
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return singletonList.iterator();
    }

    @Override
    public Object[] toArray() {
        return singletonList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return singletonList.toArray(a);
    }

    @Override
    public String toString() {
        return "SingletonKit[size=1, key=" + key + ", element=" + element + "]";
    }
}
