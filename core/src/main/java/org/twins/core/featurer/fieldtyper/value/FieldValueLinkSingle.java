package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLinkSingle extends FieldValueItem<TwinEntity> {
    @Getter
    private TwinEntity dstTwin;

    public FieldValueLinkSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueLinkSingle setDstTwin(TwinEntity dstTwin) {
        this.dstTwin = setItemWithNullifSupport(dstTwin);
        return this;
    }

    @Override
    protected TwinEntity getItem() {
        return dstTwin;
    }

    @Override
    protected Function<TwinEntity, UUID> itemGetIdFunction() {
        return TwinEntity::getId;
    }

    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueLinkSingle clone = new FieldValueLinkSingle(newTwinClassFieldEntity);
        clone.setDstTwin(this.getDstTwin());
        return clone;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        dstTwin = ((FieldValueLinkSingle) src).dstTwin;
    }

    @Override
    public void onUndefine() {
        dstTwin = null;
    }

    @Override
    public void onClear() {
        dstTwin = null;
    }
}
