package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueCalcChildrenField extends FieldValue {
    private Double value;
//todo if this class will universal double-value in future, m.b. create
// field int(decimal palces) and create method that do rounding to n decimal places?
    @Override
    public FieldValue clone() {
        FieldValueCalcChildrenField clone = new FieldValueCalcChildrenField();
        clone
                .setValue(this.value)
                .setTwinClassField(this.getTwinClassField());
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        try {
            return Objects.equals(this.value, Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
