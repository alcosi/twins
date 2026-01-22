package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLinkSingle extends FieldValue {
    private TwinEntity dstTwin;

    public FieldValueLinkSingle(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return !ObjectUtils.isEmpty(dstTwin);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueLinkSingle clone = new FieldValueLinkSingle(newTwinClassFieldEntity);
        clone.setDstTwin(this.getDstTwin());
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
        return dstTwin.getId() != null && dstTwin.getId().equals(valueUUID);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        dstTwin = ((FieldValueLinkSingle) src).dstTwin;
    }

    @Override
    public void nullify() {
        dstTwin = null;
    }

    @Override
    public boolean isNullified() {
        return dstTwin == null; //not sure that this is correct
    }
}
