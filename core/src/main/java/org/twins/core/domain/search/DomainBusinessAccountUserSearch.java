package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DomainBusinessAccountUserSearch {
    public Set<UUID> userIdList;
    public Set<UUID> userIdExcludeList;
    public Set<UUID> businessAccountIdList;
    public Set<UUID> businessAccountIdExcludeList;
    public Set<UUID> userGroupIdList;
    public Set<UUID> userGroupIdExcludeList;
    public DataTimeRange lastActivityAtRange;
    public DataTimeRange createdAtRange;

    public DomainBusinessAccountUserSearch addUserId(UUID userId, boolean exclude) {
        if (exclude)
            userIdExcludeList = CollectionUtils.safeAdd(userIdExcludeList, userId);
        else
            userIdList = CollectionUtils.safeAdd(userIdList, userId);
        return this;
    }

    public DomainBusinessAccountUserSearch addBusinessAccountId(UUID businessAccountId, boolean exclude) {
        if (exclude)
            businessAccountIdExcludeList = CollectionUtils.safeAdd(businessAccountIdExcludeList, businessAccountId);
        else
            businessAccountIdList = CollectionUtils.safeAdd(businessAccountIdList, businessAccountId);
        return this;
    }
}
