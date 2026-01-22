package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueBoolean extends FieldValue {

    private Boolean value;

    public FieldValueBoolean(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    public boolean isFilled() {
        return value != null;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueBoolean clone = new FieldValueBoolean(newTwinClassFieldEntity);
        clone.value = this.value;
        return clone;
    }

    @Override
    public void nullify() {
        value = null;
    }

    @Override
    public boolean isNullified() {
        return value == null;
    }

    @Override
    public boolean hasValue(String value) {
        return this.value == Boolean.parseBoolean(value);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        value = ((FieldValueBoolean) src).getValue();
    }
}
