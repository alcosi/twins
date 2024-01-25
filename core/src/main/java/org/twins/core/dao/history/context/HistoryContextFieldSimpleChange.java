package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;

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
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        vars.put("fromValue", fromValue != null ? fromValue : "");
        vars.put("toValue", toValue != null ? toValue : "");
        return vars;
    }
}
