package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueId extends FieldValue {
    @Getter
    private UUID id;

    public FieldValueId(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValue setId(UUID newId) {
        if (newId == null || UuidUtils.isNullifyMarker(newId)) {
            this.state = State.CLEARED;
            this.id = null;
        } else {
            this.id = newId;
            this.state = State.PRESENT;
        }
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueId clone = new FieldValueId(newTwinClassFieldEntity);
        clone.setId(this.getId());
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return UuidUtils.equals(id, value);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        id = ((FieldValueId) src).id;
    }

    @Override
    public void onUndefine() {
        id = null;
    }

    @Override
    public void onClear() {
        id = null;
    }
}
