package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldUserChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.user";
    private UUID fromUserId;
    private UUID toUserId;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        vars.put("fromUser.id", fromUserId != null ? fromUserId.toString() : "");
        vars.put("toUser.id", toUserId != null ? toUserId.toString() : "");
        return vars;
    }
}
