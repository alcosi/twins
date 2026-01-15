package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.dto.rest.LongRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SchedulerLogSearchV1")
public class SchedulerLogSearchDTOv1 {

    @Schema(description = "id set")
    public Set<UUID> idSet;

    @Schema(description = "id exclude set")
    public Set<UUID> idExcludeSet;

    @Schema(description = "scheduler id set")
    public Set<UUID> schedulerIdSet;

    @Schema(description = "scheduler id exclude set")
    public Set<UUID> schedulerIdExcludeSet;

    @Schema(description = "created at")
    public DataTimeRange createdAt;

    @Schema(description = "result like set")
    public Set<String> resultLikeSet;

    @Schema(description = "result not like set")
    public Set<String> resultNotLikeSet;

    @Schema(description = "execution time range")
    public LongRangeDTOv1 executionTimeRange;
}
