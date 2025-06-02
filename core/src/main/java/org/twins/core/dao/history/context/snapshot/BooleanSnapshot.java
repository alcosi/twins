package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class BooleanSnapshot {
    private boolean value;

    public static BooleanSnapshot convertEntity(boolean value) {
        return new BooleanSnapshot().setValue(value);
    }

    public static void extractTemplateVars(HashMap<String, String> vars, BooleanSnapshot booleanSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "value", booleanSnapshot != null ? String.valueOf(booleanSnapshot.value) : "");
    }
}
