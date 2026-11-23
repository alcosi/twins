package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

/**
 * A single data list option. Not wired to any typer (select fields are multi-option FieldValueSelect);
 * used where one option must be carried as a value of its own (e.g. a twin group key).
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueSelectSingle extends FieldValueItem<DataListOptionEntity> {
    public FieldValueSelectSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueSelectSingle setValue(DataListOptionEntity newValue) {
        return (FieldValueSelectSingle) super.setValue(newValue);
    }

    @Override
    protected Function<DataListOptionEntity, UUID> itemGetIdFunction() {
        return DataListOptionEntity::getId;
    }

    public FieldValueSelectSingle newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueSelectSingle(newTwinClassFieldEntity);
    }
}
