package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class HistorySearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinIdList;
    private Set<UUID> twinIdExcludeList;
    private boolean includeDirectChildren = false;
    private Set<UUID> actorUseridList;
    private Set<UUID> actorUserIdExcludeList;
    private Set<HistoryType> typeList;
    private Set<HistoryType> typeExcludeList;
    private DataTimeRange createdAt;
}
