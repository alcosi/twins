package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class FieldValueSimple<T> extends FieldValue {
    @Getter
    protected T value;

    public FieldValue setValue(T newValue) {
        if (newValue == null) {
            this.value = null;
            this.state = State.CLEARED;
        } else {
            this.value = newValue;
            this.state = State.PRESENT;
        }
        return this;
    }

    public FieldValueSimple(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }


    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(this.value != null ? this.value.toString() : null, value); //todo
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        value = (T) ((FieldValueSimple) src).getValue();
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
