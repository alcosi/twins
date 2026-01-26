package org.twins.core.featurer.fieldtyper.value;

import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

public abstract class FieldValueItem<T> extends FieldValue {
    public FieldValueItem(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public T setItemWithNullifSupport(T newItem) {
        if (newItem == null || UuidUtils.isNullifyMarker(itemGetIdFunction().apply(newItem))) {
            state = State.CLEARED;
            return null;
        } else {
            state = State.PRESENT;
            return newItem;
        }
    }

    @Override
    public boolean hasValue(String value) {
        if (getItem() == null)
            return false;
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        return valueUUID.equals(itemGetIdFunction().apply(getItem()));
    }

    protected abstract T getItem();

    protected abstract Function<T, UUID> itemGetIdFunction();
}
