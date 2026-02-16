package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLinkSingle extends FieldValueItem<TwinEntity> {
    public FieldValueLinkSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueLinkSingle setValue(TwinEntity newStatus) {
        return (FieldValueLinkSingle) super.setValue(newStatus);
    }

    @Override
    protected Function<TwinEntity, UUID> itemGetIdFunction() {
        return TwinEntity::getId;
    }

    public FieldValueLinkSingle newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueLinkSingle(newTwinClassFieldEntity);
    }
}
