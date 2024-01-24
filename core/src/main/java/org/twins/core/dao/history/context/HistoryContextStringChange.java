package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

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
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("fromValue", fromValue != null ? fromValue : "");
        vars.put("toValue", toValue != null ? toValue : "");
        return vars;
    }
}
