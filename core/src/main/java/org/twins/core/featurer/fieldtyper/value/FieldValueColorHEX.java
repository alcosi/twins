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

    public FieldValueColorHEX(TwinClassFieldEntity twinClassField, boolean filled) {
        super(twinClassField, filled);
    }

    @Override
    public FieldValueColorHEX clone() {
        FieldValueColorHEX clone = new FieldValueColorHEX(twinClassField, filled);
        clone.setHex(this.hex);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(hex, value);
    }
}
