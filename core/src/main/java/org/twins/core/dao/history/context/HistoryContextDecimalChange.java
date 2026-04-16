package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
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
