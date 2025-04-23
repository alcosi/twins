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
public class FieldValueDate extends FieldValue {
    private String date;

    public FieldValueDate(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return date != null;
    }

    @Override
    public FieldValueDate clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueDate clone = new FieldValueDate(newTwinClassFieldEntity);
        clone.setDate(this.date);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(date, value);
    }

    @Override
    public void nullify() {
        date = "";
    }

    @Override
    public boolean isNullified() {
        return "".equals(date);
    }
}
