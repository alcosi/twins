package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueId extends FieldValue {
    private UUID id;

    public FieldValueId(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return !ObjectUtils.isEmpty(id);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueId clone = new FieldValueId(newTwinClassFieldEntity);
        clone.setId(this.getId());
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
        return id != null && id.equals(valueUUID);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        id = ((FieldValueId) src).id;
    }

    @Override
    public void nullify() {
        id = null;
    }

    @Override
    public boolean isNullified() {
        return id == null;
    }
}
