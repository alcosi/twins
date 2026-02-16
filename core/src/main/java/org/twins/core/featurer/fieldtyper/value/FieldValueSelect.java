package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;


@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueSelect extends FieldValueCollection<DataListOptionEntity> {
    public FieldValueSelect(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected Function<DataListOptionEntity, UUID> itemGetIdFunction() {
        return DataListOptionEntity::getId;
    }

    @Override
    public FieldValueSelect newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueSelect(newTwinClassFieldEntity);
    }
}
