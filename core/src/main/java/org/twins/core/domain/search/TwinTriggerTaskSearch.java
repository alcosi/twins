package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinTriggerTaskSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinIdList;
    private Set<UUID> twinIdExcludeList;
    private Set<UUID> twinTriggerIdList;
    private Set<UUID> twinTriggerIdExcludeList;
    private Set<UUID> previousTwinStatusIdList;
    private Set<UUID> previousTwinStatusIdExcludeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<TwinTriggerTaskStatus> statusIdList;
    private Set<TwinTriggerTaskStatus> statusIdExcludeList;
}
