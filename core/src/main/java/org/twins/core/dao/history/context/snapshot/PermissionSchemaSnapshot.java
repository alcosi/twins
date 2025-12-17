package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.permission.PermissionSchemaEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PermissionSchemaSnapshot {
    private UUID id;
    private String name;

    public static PermissionSchemaSnapshot convertEntity(PermissionSchemaEntity permissionSchemaEntity) {
        if (permissionSchemaEntity == null)
            return null;
        return new PermissionSchemaSnapshot()
                .setId(permissionSchemaEntity.getId())
                .setName(permissionSchemaEntity.getName());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, PermissionSchemaSnapshot permissionSchemaSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", permissionSchemaSnapshot != null ? permissionSchemaSnapshot.id.toString() : "");
        vars.put(prefix + "name", permissionSchemaSnapshot != null ? permissionSchemaSnapshot.name : "");
    }
}
