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
        if (StringUtils.isEmpty(newValue)) {
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
        if (StringUtils.isEmpty(newValue)) {
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
    public FieldValueDate newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueDate(newTwinClassFieldEntity);
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(dateStr, value);
    }

    @Override
    public void copyValueTo(FieldValueStated dst) {
        var dstValue = (FieldValueDate) dst;
        dstValue.dateStr = dateStr;
        dstValue.date = date;
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
