package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.user.UserEntity;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class UserSnapshot {
    private String userId;
    private String name;
    private String email;

    public static UserSnapshot convertEntity(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return new UserSnapshot()
                .setUserId(userEntity.getId().toString())
                .setName(userEntity.getName())
                .setEmail(userEntity.getEmail());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, UserSnapshot userSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", userSnapshot != null ? userSnapshot.toString() : "");
        vars.put(prefix + "userId", userSnapshot != null ? userSnapshot.getUserId() : "");
        vars.put(prefix + "name", userSnapshot != null ? userSnapshot.name : ""); //todo mask for GDPR
        vars.put(prefix + "email", userSnapshot != null ? userSnapshot.email : ""); //todo mask for GDPR
    }
}
