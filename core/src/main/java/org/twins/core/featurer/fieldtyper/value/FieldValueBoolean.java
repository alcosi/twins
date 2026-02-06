package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueBoolean extends FieldValueSimple<Boolean> {

    public FieldValueBoolean(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    public FieldValueBoolean setValue(Boolean newValue) {
        return (FieldValueBoolean) super.setValue(newValue);
    }

    @Override
    public FieldValueBoolean clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueBoolean clone = new FieldValueBoolean(newTwinClassFieldEntity);
        clone.setValue(this.value);
        return clone;
    }
}
