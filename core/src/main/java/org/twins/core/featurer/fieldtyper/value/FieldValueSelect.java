package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueSelect extends FieldValueCollection<DataListOptionEntity> {
    private List<DataListOptionEntity> options = null;

    public FieldValueSelect(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected Function<DataListOptionEntity, UUID> itemGetIdFunction() {
        return DataListOptionEntity::getId;
    }

    @Override
    public FieldValueSelect clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueSelect clone = new FieldValueSelect(newTwinClassFieldEntity);
        clone.setItems(this.collection);
        return clone;
    }
}
