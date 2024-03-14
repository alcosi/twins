package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAttachment extends FieldValue {
    @Override
    public FieldValue clone() {
        FieldValueAttachment clone = new FieldValueAttachment();
        clone.setTwinClassField(this.getTwinClassField());
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return false;
    }
}
