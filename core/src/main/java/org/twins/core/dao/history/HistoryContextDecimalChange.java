package org.twins.core.dao.history;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.HistoryContext;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class HistoryContextDecimalChange extends HistoryContext {

    public static final String DISCRIMINATOR = "history.decimalChange";
    private BigDecimal fromValue;
    private BigDecimal toValue;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    public String templateFromValue() {
        return fromValue != null ? fromValue.toString() : "";
    }

    @Override
    public String templateToValue() {
        return toValue != null ? toValue.toString() : "";
    }
}
