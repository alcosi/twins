package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueId extends FieldValueSimple<UUID> {
    public FieldValueId(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueId setValue(UUID newValue) {
        return (FieldValueId) super.setValue(newValue);
    }

    @Override
    public FieldValueId newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueId(newTwinClassFieldEntity);
    }
}
