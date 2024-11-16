package org.cambium.common.kit;

import org.cambium.common.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Kit<E, K> implements Collection<E> {
    protected Collection<E> collection;
    protected Map<K, E> map;
    protected final Function<? super E, ? extends K> functionGetId;

    public Kit(Collection<E> collection, Function<? super E, ? extends K> functionGetId) {
        this.collection = collection;
        this.functionGetId = functionGetId;
    }

    public Kit(Function<? super E, ? extends K> functionGetId) {
        this.functionGetId = functionGetId;
    }

    public Collection<E> getCollection() {
        if (collection != null)
            return collection;
        return Collections.EMPTY_LIST;
    }

    public List<E> getList() {
        if (collection == null)
            return Collections.EMPTY_LIST;
        if (collection instanceof List)
            return (List<E>) collection;
        else
            return new ArrayList<>(collection);
    }

    public boolean add(E e) {
        if (collection == null)
            collection = new ArrayList<>();
        boolean ret = collection.add(e);
        if (map != null) {
            map.put(functionGetId.apply(e), e);
        }
        return ret;
    }

    public boolean addAll(Collection<? extends E> e) {
        if (collection == null)
            collection = new ArrayList<>();
        boolean ret = collection.addAll(e);
        map = null; //invalidate
        return ret;
    }

    public Map<K, E> getMap() {
        if (map != null)
            return map;
        if (collection == null)
            return Collections.EMPTY_MAP;
        map = collection
                .stream().collect(Collectors.toMap(functionGetId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        return map;
    }

    public boolean containsKey(K key) {
        getMap();
        if (map == null)
            return false;
        return map.containsKey(key);
    }

    public E get(K key) {
        getMap();
        if (map == null)
            return null;
        return map.get(key);
    }

    public Set<K> getIdSet() {
        if (map == null)
            getMap();
        if (map == null)
            return null;
        return map.keySet();
    }

    @Override
    public String toString() {
        return "size = " + (collection != null ? CollectionUtils.size(collection) : "0");
    }

    //collection stuff

    @Override
    public boolean remove(Object o) {
        boolean ret =  collection.remove(o);
        if (map != null) {
            map.remove(functionGetId.apply((E) o));
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = collection.removeAll(c);
        if (map != null) {
            for (Object o : c) {
                map.remove(functionGetId.apply((E) o));
            }
        }
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        map = null; //invalidating
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        map = null; //invalidating
        collection.clear();
    }

    @Override
    public int size() {
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
        return collection != null ? collection.toArray() : null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection != null ? collection.toArray(a) : null;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    private class KitIterator implements Iterator<E> {
        Iterator<E> collectionIterator = collection != null ? collection.iterator() : Collections.emptyIterator();;
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
            if (map != null)
                map.remove(functionGetId.apply(currentItem));
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            collectionIterator.forEachRemaining(action);
        }
    }
}
