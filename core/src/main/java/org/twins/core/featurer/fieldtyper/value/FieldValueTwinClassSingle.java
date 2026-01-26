package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;


@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassSingle extends FieldValueItem<TwinClassEntity> {
    @Getter
    private TwinClassEntity twinClass;

    public FieldValueTwinClassSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected TwinClassEntity getItem() {
        return twinClass;
    }

    @Override
    protected Function<TwinClassEntity, UUID> itemGetIdFunction() {
        return TwinClassEntity::getId;
    }

    public FieldValueTwinClassSingle setTwinClass(TwinClassEntity newTwinClass) {
        twinClass = setItemWithNullifSupport(newTwinClass);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassSingle clone = new FieldValueTwinClassSingle(newTwinClassFieldEntity);
        clone.setTwinClass(this.getTwinClass());
        return clone;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        twinClass = ((FieldValueTwinClassSingle) src).twinClass;
    }

    @Override
    public void onUndefine() {
        twinClass = null;
    }

    @Override
    public void onClear() {
        twinClass = null;
    }
}
