package org.twins.core.domain.space;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.*;

@Data
@Accessors(chain = true)
public class UserRefSpaceRole {

    UserEntity user;
    List<SpaceRoleUserEntity> roles;

    public UserRefSpaceRole addRole(SpaceRoleUserEntity role) {
        roles = CollectionUtils.safeAdd(roles, role);
        return this;
    }

    public UserRefSpaceRole addRoles(List<SpaceRoleUserEntity> items) {
        roles = CollectionUtils.safeAdd(roles, items);
        return this;
    }

}
