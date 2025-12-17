package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.PermissionSchemaSnapshot;
import org.twins.core.dao.permission.PermissionSchemaEntity;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextPermissionSchemaChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.permissionSchemaChange";
    private PermissionSchemaSnapshot fromPermissionSchema;
    private PermissionSchemaSnapshot toPermissionSchema;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextPermissionSchemaChange shotFromPermissionSchema(PermissionSchemaEntity permissionSchemaEntity) {
        fromPermissionSchema = PermissionSchemaSnapshot.convertEntity(permissionSchemaEntity);
        return this;
    }

    public HistoryContextPermissionSchemaChange shotToPermissionSchema(PermissionSchemaEntity permissionSchemaEntity) {
        toPermissionSchema = PermissionSchemaSnapshot.convertEntity(permissionSchemaEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        PermissionSchemaSnapshot.extractTemplateVars(vars, fromPermissionSchema, "fromPermissionSchema");
        PermissionSchemaSnapshot.extractTemplateVars(vars, toPermissionSchema, "toPermissionSchema");
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromPermissionSchema != null ? fromPermissionSchema.getName() : "";
    }

    @Override
    public String templateToValue() {
        return toPermissionSchema != null ? toPermissionSchema.getName() : "";
    }
}
