package org.twins.core.featurer.fieldtyper.value;

import lombok.Getter;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

public abstract class FieldValueItem<T> extends FieldValueStated {
    @Getter
    protected T value;

    public FieldValueItem(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueItem<T> setValue(T newStatus) {
        this.value = setItemWithNullifSupport(newStatus);
        return this;
    }

    public  T setItemWithNullifSupport(T newItem) {
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
        if (this.value == null)
            return false;
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        return valueUUID.equals(itemGetIdFunction().apply(this.value));
    }

    protected abstract Function<T, UUID> itemGetIdFunction();

    @Override
    public void copyValueTo(FieldValueStated dst) {
        ((FieldValueItem<T>) dst).value = value;
    }

    @Override
    public void onUndefine() {
        value = null;
    }

    @Override
    public void onClear() {
        value = null;
    }
}
