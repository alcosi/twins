package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassList extends FieldValue {

    private Set<TwinClassEntity> twinClassEntities = new HashSet<>();

    public FieldValueTwinClassList(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    public boolean isFilled() {
        return CollectionUtils.isNotEmpty(twinClassEntities);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassList clone = new FieldValueTwinClassList(newTwinClassFieldEntity);
        clone.twinClassEntities = this.twinClassEntities;
        return clone;
    }

    @Override
    public void nullify() {
        twinClassEntities = null;
    }

    @Override
    public boolean isNullified() {
        return twinClassEntities == null;
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(twinClassEntities)) {
            return false;
        }

        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }

        for (var id : twinClassEntities) {
            if (id.equals(valueUUID))
                return true;
        }

        return false;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        twinClassEntities.clear();
        twinClassEntities.addAll(((FieldValueTwinClassList) src).getTwinClassEntities());
    }
}