package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldSearchSpaceRoleUser extends TwinFieldSearch {
    public Set<UUID> roleIdList;
    public Set<UUID> roleIdExcludeList;
    public Set<UUID> userIdList;
    public Set<UUID> userIdExcludeList;

    public boolean isEmptySearch() {
        return CollectionUtils.isEmpty(roleIdList) &&
                CollectionUtils.isEmpty(roleIdExcludeList) &&
                CollectionUtils.isEmpty(userIdList) &&
                CollectionUtils.isEmpty(userIdExcludeList);
    }
}
