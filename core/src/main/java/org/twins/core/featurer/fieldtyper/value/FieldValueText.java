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
public class FieldValueText extends FieldValue {
    private String value;

    public FieldValueText(TwinClassFieldEntity twinClassField, boolean filled) {
        super(twinClassField, filled);
    }

    @Override
    public FieldValue clone() {
        FieldValueText clone = new FieldValueText(twinClassField, filled);
        clone.setValue(this.value);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(this.value, value);
    }
}
