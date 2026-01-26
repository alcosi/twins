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
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueInvisible clone = new FieldValueInvisible(newTwinClassFieldEntity);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return false;
    }

    @Override
    public void copyValueFrom(FieldValue src) {

    }

    @Override
    public void onUndefine() {

    }

    @Override
    public void onClear() {

    }
}
