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
public class TwinFieldValueSearchSpaceRoleUser extends TwinFieldValueSearch {
    private Set<UUID> roleIdList;
    private Set<UUID> roleIdExcludeList;
    private Set<UUID> userIdList;
    private Set<UUID> userIdExcludeList;

    @Override
    public boolean isEmptySearch() {
        return CollectionUtils.isEmpty(roleIdList)
                && CollectionUtils.isEmpty(roleIdExcludeList)
                && CollectionUtils.isEmpty(userIdList)
                && CollectionUtils.isEmpty(userIdExcludeList);
    }
}

