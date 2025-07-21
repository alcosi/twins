package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassList extends FieldValue {

    private Set<UUID> twinClassIdSet = new HashSet<>();

    public FieldValueTwinClassList(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    public boolean isFilled() {
        return !twinClassIdSet.isEmpty();
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassList clone = new FieldValueTwinClassList(newTwinClassFieldEntity);
        clone.twinClassIdSet = this.twinClassIdSet;
        return clone;
    }

    @Override
    public void nullify() {
        twinClassIdSet = null;
    }

    @Override
    public boolean isNullified() {
        return twinClassIdSet == null;
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(twinClassIdSet)) {
            return false;
        }

        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }

        for (var id : twinClassIdSet) {
            if (id.equals(valueUUID))
                return true;
        }

        return false;
    }
}