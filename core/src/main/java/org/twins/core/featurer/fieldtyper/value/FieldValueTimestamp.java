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
public class FieldValueTimestamp extends FieldValue {
    private String value;

    public FieldValueTimestamp(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return value != null && !value.isEmpty();
    }

    @Override
    public FieldValueTimestamp clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTimestamp clone = new FieldValueTimestamp(newTwinClassFieldEntity);
        clone.setValue(this.value);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(this.value, value);
    }

    @Override
    public void nullify() {
        value = null;
    }

    @Override
    public boolean isNullified() {
        return value == null;
    }
}
