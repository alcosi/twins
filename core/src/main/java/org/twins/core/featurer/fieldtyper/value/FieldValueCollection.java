package org.twins.core.featurer.fieldtyper.value;

import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.*;
import java.util.function.Function;


public abstract class FieldValueCollection<T> extends FieldValue {
    private List<T> collection = null;

    public FieldValueCollection(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public List<T> getItems() {
        return collection != null
                ? Collections.unmodifiableList(collection)
                : Collections.emptyList();
    }

    protected List<T> setWithNullifyMarkerSupport(Collection<T> newCollection) {
        if (CollectionUtils.isEmpty(newCollection) || UuidUtils.hasNullifyMarker(newCollection, itemGetIdFunction())) {
            state = FieldValue.State.CLEARED;
            return null;
        } else {
            state = FieldValue.State.PRESENT;
            return new ArrayList<>(newCollection); //this will be another list
        }
    }

    public FieldValueCollection<T> add(T newItem) {
        if (newItem == null) {
            return this;
        } else if (UuidUtils.isNullifyMarker(itemGetIdFunction().apply(newItem))) {
            state = State.CLEARED;
            collection = null;
        } else {
            state = State.PRESENT;
            CollectionUtils.safeAdd(collection, newItem);
        }
        return this;
    }

    protected List<T> addWithNullifyMarkerSupport(List<T> collection, T newItem) {
        if (newItem == null) {
            return collection;
        } else if (UuidUtils.isNullifyMarker(itemGetIdFunction().apply(newItem))) {
            state = State.CLEARED;
            return null;
        }
        CollectionUtils.safeAdd(collection, newItem);
        state = State.PRESENT;
        return collection;
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
            collection.addAll(((FieldValueCollection<T>) src).c);
        }

    }


    protected abstract Function<T, UUID> itemGetIdFunction();
}
