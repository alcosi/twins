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
public class FieldValueBase extends FieldValue {
    private String value;

    public FieldValueBase(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return value != null;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueBase clone = new FieldValueBase(newTwinClassFieldEntity);
        clone.setValue(this.value);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(this.value, value);
    }

    public void nullify() {
        value = "";
    }

}
