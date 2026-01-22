package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueSelect extends FieldValue {
    private List<DataListOptionEntity> options = new ArrayList<>();

    public FieldValueSelect(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return CollectionUtils.isNotEmpty(options);
    }

    public FieldValueSelect add(DataListOptionEntity option) {
        options.add(option);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueSelect clone = new FieldValueSelect(newTwinClassFieldEntity);
        clone.getOptions().addAll(this.options);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(options)) {
            return false;
        }
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        for (DataListOptionEntity dataListOptionEntity : options) {
            if (dataListOptionEntity.getId() != null && dataListOptionEntity.getId().equals(valueUUID))
                return true;
        }
        return false;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        options.clear();
        options.addAll(((FieldValueSelect) src).options);
    }

    public void nullify() {
        options = Collections.EMPTY_LIST;
    }

    @Override
    public boolean isNullified() {
        return options != null && options.isEmpty();
    }

}
