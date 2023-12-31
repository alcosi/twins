package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueColorHEX extends FieldValue {
    private String hex;

    @Override
    public FieldValueColorHEX clone() {
        FieldValueColorHEX clone = new FieldValueColorHEX();
        clone
                .setHex(this.hex)
                .setTwinClassField(this.getTwinClassField());
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(hex, value);
    }
}
