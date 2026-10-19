package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.Set;
import java.util.UUID;

/**
 * Top-level (not tied to a twin class field) search criterion filtering twins by space_role_user membership.
 * Twin matches if it has a space_role_user row with {@link #spaceRoleId} and one of {@link #userIdList} users.
 * Several criteria in {@link TwinSearch#getSpaceRoleUsersList()} are OR-joined.
 */
@Data
@Accessors(chain = true)
public class TwinSearchBySpaceRoleUser {
    private UUID spaceRoleId;
    private Set<UUID> userIdList;

    public boolean isEmpty() {
        return spaceRoleId == null && CollectionUtils.isEmpty(userIdList);
    }
}
