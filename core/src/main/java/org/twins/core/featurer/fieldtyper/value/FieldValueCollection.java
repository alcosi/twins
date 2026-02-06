package org.twins.core.featurer.fieldtyper.value;

import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.*;
import java.util.function.Function;

/**
 * Collection can be updated outside an object.
 * So isCleared and isUndefined are detected on the fly (based on a collection)
 * @param <T>
 */
public abstract class FieldValueCollection<T> extends FieldValue {
    protected List<T> collection = null;

    public FieldValueCollection(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public List<T> getItems() {
        return collection;
    }

    public List<T> getItemsOrEmpty() {
        return collection != null
                ? collection
                : Collections.emptyList();
    }

    public FieldValueCollection<T> add(T newItem) {
        if (newItem == null) {
            return this;
        } else if (UuidUtils.isNullifyMarker(itemGetIdFunction().apply(newItem))) {
            clear();
        } else {
            collection = CollectionUtils.safeAdd(collection, newItem);
        }
        return this;
    }

    public FieldValueCollection<T> setItems(Collection<T> newCollection) {
        if (CollectionUtils.isEmpty(newCollection) || UuidUtils.hasNullifyMarker(newCollection, itemGetIdFunction())) {
            clear();
        } else {
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
    public void copyValueFrom(FieldValue src) {
        if (collection != null) {
            collection.clear();
            collection.addAll(((FieldValueCollection<T>) src).collection);
        }
    }

    @Override
    public boolean isUndefined() {
        return collection == null;
    }

    @Override
    public boolean isCleared() {
        return collection != null && collection.isEmpty();
    }

    @Override
    public FieldValue undefine() {
        collection = null; // this will be check in isUndefined
        return this;
    }

    @Override
    public FieldValue clear() {
        if (collection != null) {
            collection.clear();
        } else {
            collection = new ArrayList<>(); // we do not use Collections.emptyList(), because it's immutable
        }
        return null;
    }

    protected abstract Function<T, UUID> itemGetIdFunction();
}
