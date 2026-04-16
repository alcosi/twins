package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.DateUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueDate extends FieldValueStated {
    @Getter
    private LocalDateTime date;
    private String pattern;

    public FieldValueDate(TwinClassFieldEntity twinClassField, String pattern) {
        super(twinClassField);
        this.pattern = pattern;
    }

    public String getDateStr() {
        return DateUtils.formatDate(date, pattern);
    }

    public FieldValueDate setDate(String dateStr) throws ServiceException {
        if (StringUtils.isEmpty(dateStr)) {
            this.date = null;
            this.state = State.CLEARED;
        } else {
            this.date = DateUtils.parseDateTime(dateStr, pattern);
            this.state = State.PRESENT;
        }
        return this;
    }

    public FieldValueDate setDate(LocalDateTime date) {
        if (date == null) {
            this.date = null;
            this.state = State.CLEARED;
        } else {
            this.date = date;
            this.state = State.PRESENT;
        }
        return this;
    }

    @Override
    public FieldValueDate newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueDate(newTwinClassFieldEntity, null);
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(getDateStr(), value);
    }

    @Override
    public void copyValueTo(FieldValueStated dst) {
        var dstValue = (FieldValueDate) dst;
        dstValue.pattern = pattern;
        dstValue.date = date;
    }

    @Override
    public void onUndefine() {
        date = null;
    }

    @Override
    public void onClear() {
        date = null;
    }
}
