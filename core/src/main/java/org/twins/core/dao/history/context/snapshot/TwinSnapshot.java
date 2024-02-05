package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinSnapshot {
    private UUID id;
    private String name;
    private String alias;

    public static TwinSnapshot convertEntity(TwinEntity twinEntity) {
        if (twinEntity == null)
            return null;
        return new TwinSnapshot()
                .setId(twinEntity.getId())
                .setName(twinEntity.getName())
                .setAlias("TWIN-108"); //todo fix it
    }

    public static void extractTemplateVars(HashMap<String, String> vars, TwinSnapshot twinSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", twinSnapshot != null ? twinSnapshot.id.toString() : "");
        vars.put(prefix + "name", twinSnapshot != null ? twinSnapshot.name : "");
        vars.put(prefix + "alias", twinSnapshot != null ? twinSnapshot.alias : "");
    }
}
