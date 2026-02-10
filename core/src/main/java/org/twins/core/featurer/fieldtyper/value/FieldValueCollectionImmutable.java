package org.twins.core.featurer.fieldtyper.value;

import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.*;
import java.util.function.Function;

/**
 * An immutable version of a collection
 * @param <T>
 */
public abstract class FieldValueCollectionImmutable<T> extends FieldValueStated {
    protected List<T> collection = null;

    public FieldValueCollectionImmutable(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public List<T> getItems() {
        return collection != null
                ? Collections.unmodifiableList(collection)
                : Collections.emptyList();
    }


    public FieldValueCollectionImmutable<T> add(T newItem) {
        if (newItem == null) {
            return this;
        } else if (UuidUtils.isNullifyMarker(itemGetIdFunction().apply(newItem))) {
            state = State.CLEARED;
            collection = null;
        } else {
            state = State.PRESENT;
            collection = CollectionUtils.safeAdd(collection, newItem);
        }
        return this;
    }

    public FieldValueCollectionImmutable<T> setItems(Collection<T> newCollection) {
        if (CollectionUtils.isEmpty(newCollection) || UuidUtils.hasNullifyMarker(newCollection, itemGetIdFunction())) {
            state = State.CLEARED;
            collection = null;
        } else {
            state = State.PRESENT;
            collection = new ArrayList<>(newCollection); //this will be another list
        }
        return this;
    }


    public int size() {
        return collection == null ? 0 : collection.size();
    }

    @Override
    public boolean hasValue(String value) {
        return UuidUtils.hasValue(collection, value, itemGetIdFunction());
    }

    @Override
    public void copyValueTo(FieldValueStated dst) {
        var dstValue = (FieldValueCollectionImmutable<T>) dst;
        if (dstValue.collection != null) {
            dstValue.collection.clear();
            dstValue.collection.addAll(collection);
        } else {
            dstValue.collection = new ArrayList<>(collection);
        }
    }

    @Override
    public void onUndefine() {
        collection = null;
    }

    @Override
    public void onClear() {
        collection = null;
    }

    protected abstract Function<T, UUID> itemGetIdFunction();
}
