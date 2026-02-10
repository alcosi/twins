package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAliases extends FieldValueCollectionImmutable<TwinAliasEntity> {
    public FieldValueAliases(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected Function<TwinAliasEntity, UUID> itemGetIdFunction() {
        return TwinAliasEntity::getId;
    }

    @Override
    public FieldValueAliases newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueAliases(newTwinClassFieldEntity);
    }
}
