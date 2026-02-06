package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueStatus extends FieldValueItem<TwinStatusEntity> {
    public FieldValueStatus(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueStatus setValue(TwinStatusEntity newStatus) {
        return (FieldValueStatus) super.setValue(newStatus);
    }

    @Override
    protected Function<TwinStatusEntity, UUID> itemGetIdFunction() {
        return TwinStatusEntity::getId;
    }

    @Override
    public FieldValueStatus clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueStatus clone = new FieldValueStatus(newTwinClassFieldEntity);
        clone.setValue(this.getValue());
        return clone;
    }


}
