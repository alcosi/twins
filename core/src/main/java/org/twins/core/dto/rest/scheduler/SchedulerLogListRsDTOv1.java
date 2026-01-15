package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SchedulerLogListRsV1")
public class SchedulerLogListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - scheduler log list")
    public List<SchedulerLogDTOv1> schedulerLogs;
}
