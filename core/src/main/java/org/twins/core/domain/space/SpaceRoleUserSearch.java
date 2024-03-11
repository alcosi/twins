package org.twins.core.domain.space;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.*;

@Data
@Accessors(chain = true)
public class SpaceRoleUserSearch {

    String userNameLike;
    Set<UUID> spaceRolesList;
    Set<UUID> userGroupIdList;


    public SpaceRoleUserSearch addRoleId(UUID roleId) {
        spaceRolesList = CollectionUtils.safeAdd(spaceRolesList, roleId);
        return this;
    }

    public SpaceRoleUserSearch addGroupId(UUID roleId) {
        userGroupIdList = CollectionUtils.safeAdd(userGroupIdList, roleId);
        return this;
    }
}
