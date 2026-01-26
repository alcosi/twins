package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueDate extends FieldValue {
    private String dateStr;
    private LocalDateTime date;

    public FieldValueDate(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return dateStr != null;
    }

    @Override
    public FieldValueDate clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueDate clone = new FieldValueDate(newTwinClassFieldEntity);
        clone.setDateStr(this.dateStr);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(dateStr, value);
    }

    @Override
    public void nullify() {
        dateStr = "";
    }

    @Override
    public boolean isNullified() {
        return "".equals(dateStr);
    }
}
