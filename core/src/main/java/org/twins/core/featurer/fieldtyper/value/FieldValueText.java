package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueText extends FieldValue {
    private String value;

    @Override
    public FieldValue clone() {
        FieldValueText clone = new FieldValueText();
        clone
                .setValue(this.value)
                .setTwinClassField(this.getTwinClassField());
        return clone;
    }
}
