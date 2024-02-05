package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldSimpleChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.simple";
    private String fromValue;
    private String toValue;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    public String getTemplateFromValue() {
        return fromValue;
    }

    @Override
    public String getTemplateToValue() {
        return toValue;
    }
}
