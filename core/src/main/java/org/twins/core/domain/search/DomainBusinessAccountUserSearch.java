package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountUserSortField;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DomainBusinessAccountUserSearch {
    private Set<UUID> userIdList;
    private Set<UUID> userIdExcludeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<UUID> userGroupIdList;
    private Set<UUID> userGroupIdExcludeList;
    private DataTimeRange lastActivityAtRange;
    private DataTimeRange createdAtRange;
    private DomainBusinessAccountUserSortField sortField = DomainBusinessAccountUserSortField.createdAt;
    private SortDirection sortDirection = SortDirection.ASC;

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
