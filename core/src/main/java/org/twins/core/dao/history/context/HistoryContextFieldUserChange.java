package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.UserSnapshot;
import org.twins.core.dao.user.UserEntity;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldUserChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.user";
    private UserSnapshot fromUser;
    private UserSnapshot toUser;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        UserSnapshot.extractTemplateVars(vars, fromUser, "fromUser");
        UserSnapshot.extractTemplateVars(vars, toUser, "toUser");
        return vars;
    }

    @Override
    public String getTemplateFromValue() {
        return fromUser != null ? fromUser.getName() : "";
    }

    @Override
    public String getTemplateToValue() {
        return toUser != null ? toUser.getName() : "";
    }

    public HistoryContextFieldUserChange shotFromUser(UserEntity userEntity) {
        fromUser = UserSnapshot.convertEntity(userEntity);
        return this;
    }

    public HistoryContextFieldUserChange shotToUser(UserEntity userEntity) {
        toUser = UserSnapshot.convertEntity(userEntity);
        return this;
    }
}
