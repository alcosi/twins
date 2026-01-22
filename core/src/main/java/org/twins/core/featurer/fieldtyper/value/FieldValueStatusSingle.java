package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueStatusSingle extends FieldValue {
    private TwinStatusEntity status;

    public FieldValueStatusSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return !ObjectUtils.isEmpty(status);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueStatusSingle clone = new FieldValueStatusSingle(newTwinClassFieldEntity);
        clone.setStatus(this.getStatus());
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
        return status.getId() != null && status.getId().equals(valueUUID);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        status = ((FieldValueStatusSingle) src).status;
    }

    @Override
    public void nullify() {
        status = null;
    }

    @Override
    public boolean isNullified() {
        return status == null;
    }
}
