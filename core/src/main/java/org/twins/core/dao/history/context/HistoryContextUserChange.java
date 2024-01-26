package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.user.UserEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryContextUserChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.userChange";
    private UUID fromUserId;
    private UserDraft fromUser; //in case if user is already deleted from DB we can display this draft data
    private UUID toUserId;
    private UserDraft toUser; //in case if user is already deleted from DB we can display this draft data

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
        vars.put("fromUser.id", fromUserId != null ? fromUserId.toString() : "");
        vars.put("fromUser.name", fromUser != null ? fromUser.name : "");
        vars.put("fromUser.email", fromUser != null ? fromUser.email : "");
        vars.put("toUser.id", toUserId != null ? toUserId.toString() : "");
        vars.put("toUser.name", toUser != null ? toUser.name : "");
        vars.put("toUser.email", toUser != null ? toUser.email : "");
        return vars;
    }

    //todo delete this because of GDPR
    @Data
    @Accessors(chain = true)
    public static final class UserDraft {
        private String name;
        private String email;

        public static UserDraft convertEntity(UserEntity userEntity) {
            if (userEntity == null)
                return null;
            return new UserDraft()
                    .setName(userEntity.getName())
                    .setEmail(userEntity.getEmail());
        }
    }
}
