package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassSingle extends FieldValue {
    private TwinClassEntity twinClass;

    public FieldValueTwinClassSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return !ObjectUtils.isEmpty(twinClass);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassSingle clone = new FieldValueTwinClassSingle(newTwinClassFieldEntity);
        clone.setTwinClass(this.getTwinClass());
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
        return twinClass.getId() != null && twinClass.getId().equals(valueUUID);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        twinClass = ((FieldValueTwinClassSingle) src).twinClass;
    }

    @Override
    public void nullify() {
        twinClass = null;
    }

    @Override
    public boolean isNullified() {
        return twinClass == null;
    }
}
