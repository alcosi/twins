package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueSelect extends FieldValue {
    private List<DataListOptionEntity> options = new ArrayList<>();

    public FieldValueSelect add(DataListOptionEntity option) {
        options.add(option);
        return this;
    }

    @Override
    public FieldValue clone() {
        FieldValueSelect clone = new FieldValueSelect();
        clone
                .setTwinClassField(this.getTwinClassField());
        clone.getOptions().addAll(this.options);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        for (DataListOptionEntity dataListOptionEntity : options) {
            if (dataListOptionEntity.getId() != null &&dataListOptionEntity.getId().equals(valueUUID))
                return true;
        }
        return false;
    }
}
