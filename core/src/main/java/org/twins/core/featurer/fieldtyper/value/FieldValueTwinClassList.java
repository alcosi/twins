package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassList extends FieldValueCollectionImmutable<TwinClassEntity> {
    public FieldValueTwinClassList(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    protected Function<TwinClassEntity, UUID> itemGetIdFunction() {
        return TwinClassEntity::getId;
    }

    @Override
    public FieldValueTwinClassList newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueTwinClassList(newTwinClassFieldEntity);
    }
}