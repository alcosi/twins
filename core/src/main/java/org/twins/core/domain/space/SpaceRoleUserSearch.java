package org.twins.core.domain.space;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.*;

@Data
@Accessors(chain = true)
public class SpaceRoleUserSearch {

    String nameLike;
    Set<UUID> rolesList;


    public SpaceRoleUserSearch addRoleId(UUID roleId) {
        rolesList = CollectionUtils.safeAdd(rolesList, roleId);
        return this;
    }
}
