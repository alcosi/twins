package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueStatus extends FieldValueItem<TwinStatusEntity> {
    @Getter
    private TwinStatusEntity status;

    public FieldValueStatus(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueStatus setStatus(TwinStatusEntity newStatus) {
        this.status = setItemWithNullifSupport(newStatus);
        return this;
    }

    @Override
    protected TwinStatusEntity getItem() {
        return status;
    }

    @Override
    protected Function<TwinStatusEntity, UUID> itemGetIdFunction() {
        return TwinStatusEntity::getId;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueStatus clone = new FieldValueStatus(newTwinClassFieldEntity);
        clone.setStatus(this.getStatus());
        return clone;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        status = ((FieldValueStatus) src).status;
    }

    @Override
    public void onUndefine() {
        status = null;
    }

    @Override
    public void onClear() {
        status = null;
    }
}
