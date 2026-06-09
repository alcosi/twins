package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DomainBusinessAccountUserSearch extends EntitySearch<DomainBusinessAccountUserEntity> {
    private Set<UUID> userIdList;
    private Set<UUID> userIdExcludeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<UUID> userGroupIdList;
    private Set<UUID> userGroupIdExcludeList;
    private DataTimeRange lastActivityAtRange;
    private DataTimeRange createdAtRange;
}
