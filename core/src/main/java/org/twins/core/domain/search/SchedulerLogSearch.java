package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.dto.rest.LongRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SchedulerLogSearch {
    public Set<UUID> idSet;
    public Set<UUID> idExcludeSet;
    public Set<UUID> schedulerIdSet;
    public Set<UUID> schedulerIdExcludeSet;
    public DataTimeRangeDTOv1 createdAt;
    public Set<String> resultLikeSet;
    public Set<String> resultNotLikeSet;
    public LongRangeDTOv1 executionTimeRange;
}
