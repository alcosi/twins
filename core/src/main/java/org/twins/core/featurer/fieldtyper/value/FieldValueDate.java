package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueDate extends FieldValue {
    private String date;

    @Override
    public FieldValueDate clone() {
        FieldValueDate clone = new FieldValueDate();
        clone
                .setDate(this.date)
                .setTwinClassField(this.getTwinClassField());
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(date, value);
    }
}
