package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassSnapshot {
    private UUID id;
    private String key;

    public static TwinClassSnapshot convertEntity(TwinClassEntity twinClassEntity) {
        if (twinClassEntity == null)
            return null;
        return new TwinClassSnapshot()
                .setId(twinClassEntity.getId())
                .setKey(twinClassEntity.getKey());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, TwinClassSnapshot twinClassSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", twinClassSnapshot != null ? String.valueOf(twinClassSnapshot.getId()) : "");
        vars.put(prefix + "key", twinClassSnapshot != null ? twinClassSnapshot.getKey() : "");
    }
}
