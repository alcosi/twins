package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;


@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassSingle extends FieldValueItem<TwinClassEntity> {
    public FieldValueTwinClassSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueTwinClassSingle setValue(TwinClassEntity newStatus) {
        return (FieldValueTwinClassSingle) super.setValue(newStatus);
    }

    @Override
    protected Function<TwinClassEntity, UUID> itemGetIdFunction() {
        return TwinClassEntity::getId;
    }

    @Override
    public FieldValueTwinClassSingle clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassSingle clone = new FieldValueTwinClassSingle(newTwinClassFieldEntity);
        clone.setValue(this.getValue());
        return clone;
    }
}
