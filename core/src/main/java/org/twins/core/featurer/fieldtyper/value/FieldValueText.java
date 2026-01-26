package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueText extends FieldValueSimple<String> {
    public FieldValueText(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueText clone = new FieldValueText(newTwinClassFieldEntity);
        clone.setValue(this.value);
        return clone;
    }
}
