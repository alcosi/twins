package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueEmpty extends FieldValue {
    @Override
    public FieldValue clone() {
        FieldValueEmpty clone = new FieldValueEmpty();
        clone.setTwinClassField(this.getTwinClassField());
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return false;
    }
}
