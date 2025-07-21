package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassList extends FieldValue {

    private List<TwinClassEntity> twinClassList = new ArrayList<>();

    public FieldValueTwinClassList(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    public boolean isFilled() {
        return twinClassList != null;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassList clone = new FieldValueTwinClassList(newTwinClassFieldEntity);
        clone.twinClassList = this.twinClassList;
        return clone;
    }

    @Override
    public void nullify() {
        twinClassList = null;
    }

    @Override
    public boolean isNullified() {
        return twinClassList == null;
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(twinClassList)) {
            return false;
        }

        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }

        for (var twinClass : twinClassList) {
            if (twinClass.getId() != null && twinClass.getId().equals(valueUUID))
                return true;
        }

        return false;
    }
}