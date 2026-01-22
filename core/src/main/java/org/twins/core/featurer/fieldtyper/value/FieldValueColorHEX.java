package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueColorHEX extends FieldValue {
    private String hex;

    public FieldValueColorHEX(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return hex != null;
    }

    @Override
    public FieldValueColorHEX clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueColorHEX clone = new FieldValueColorHEX(newTwinClassFieldEntity);
        clone.setHex(this.hex);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(hex, value);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        hex = ((FieldValueColorHEX) src).getHex();
    }

    @Override
    public void nullify() {
        hex = "";
    }

    @Override
    public boolean isNullified() {
        return "".equals(hex);
    }
}
