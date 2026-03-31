package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.math.LongRange;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SchedulerLogSearch {
    public Set<UUID> idSet;
    public Set<UUID> idExcludeSet;
    public Set<UUID> schedulerIdSet;
    public Set<UUID> schedulerIdExcludeSet;
    public DataTimeRange createdAt;
    public Set<String> resultLikeSet;
    public Set<String> resultNotLikeSet;
    public LongRange executionTimeRange;
}
