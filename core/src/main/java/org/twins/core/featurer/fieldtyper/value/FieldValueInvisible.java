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
public class FieldValueInvisible extends FieldValue {

    public FieldValueInvisible(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValue newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueInvisible(newTwinClassFieldEntity);
    }

    @Override
    public boolean hasValue(String value) {
        return false;
    }

    @Override
    public void copyValueTo(FieldValue dst) {

    }

    @Override
    public FieldValueInvisible undefine() {
        return this;
    }

    @Override
    public boolean isUndefined() {
        return false;
    }

    @Override
    public FieldValueInvisible clear() {
        return null;
    }

    @Override
    public boolean isCleared() {
        return false;
    }
}
