package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.UserSnapshot;
import org.twins.core.dao.user.UserEntity;

import java.util.HashMap;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextUserChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.userChange";
    private UUID fromUserId; //can be null
    private UserSnapshot fromUser;
    private UUID toUserId;
    private UserSnapshot toUser;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Deprecated
    public HistoryContextUserChange shotFromUser(UserEntity userEntity) {
        fromUser = UserSnapshot.convertEntity(userEntity);
        return this;
    }

    @Deprecated
    public HistoryContextUserChange shotToUser(UserEntity userEntity) {
        toUser = UserSnapshot.convertEntity(userEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        UserSnapshot.extractTemplateVars(vars, fromUser , "fromUser");
        UserSnapshot.extractTemplateVars(vars, toUser , "toUser");
        return vars;
    }
    }
