package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextSpaceRoleUserChange extends HistoryContext {
    public static final String DISCRIMINATOR = "historyContextSpaceRoleUserChange";
    private UUID roleId;
    private List<UUID> targetedUserIds;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        if (roleId != null)
            vars.put("role.id", roleId.toString());
        if (targetedUserIds != null) {
            vars.put("user.count", String.valueOf(targetedUserIds.size()));
        }
        return vars;
    }

    @Override
    public String templateFromValue() {
        return "";
    }

    @Override
    public String templateToValue() {
        return "";
    }
}
