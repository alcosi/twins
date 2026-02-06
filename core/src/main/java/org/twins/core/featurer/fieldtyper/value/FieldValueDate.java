package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueDate extends FieldValueStated {
    @Getter
    private String dateStr;
    @Getter
    private LocalDateTime date;

    public FieldValueDate(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueDate setDateStr(String newValue) {
        if (newValue == null) {
            this.dateStr = null;
            this.date = null;
            this.state = State.CLEARED;
        } else {
            this.dateStr = newValue;
            this.date = null;
            this.state = State.PRESENT;
        }
        return this;
    }

    public FieldValueDate setDate(LocalDateTime date, String newValue) {
        if (newValue == null) {
            this.dateStr = null;
            this.date = null;
            this.state = State.CLEARED;
        } else {
            this.dateStr = newValue;
            this.date = date;
            this.state = State.PRESENT;
        }
        return this;
    }

    @Override
    public FieldValueDate clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueDate clone = new FieldValueDate(newTwinClassFieldEntity);
        clone.dateStr = this.dateStr;
        clone.date = this.date;
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(dateStr, value);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        dateStr = ((FieldValueDate) src).getDateStr();
        date = ((FieldValueDate) src).getDate();
    }

    @Override
    public void onUndefine() {
        dateStr = null;
        date = null;
    }

    @Override
    public void onClear() {
        dateStr = null;
        date = null;
    }
}
