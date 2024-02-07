package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HistoryContextStringChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.stringChange";
    private String fromValue;
    private String toValue;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String templateFromValue() {
        return fromValue != null ? fromValue : "";
    }

    @Override
    public String templateToValue() {
        return toValue != null ? toValue : "";
    }
}
