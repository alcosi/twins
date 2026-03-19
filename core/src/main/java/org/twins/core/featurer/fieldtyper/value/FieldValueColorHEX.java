package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueColorHEX extends FieldValueSimple<String> {
    public FieldValueColorHEX(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueColorHEX newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueColorHEX(newTwinClassFieldEntity);
    }

    @Override
    public FieldValueColorHEX setValue(String newValue) {
        return (FieldValueColorHEX) super.setValue(newValue);
    }
}
