package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SchedulerLogSearchRqV1")
public class SchedulerLogSearchRqDTOv1 extends Request {

    @Schema(description = "search DTO")
    public SchedulerLogSearchDTOv1 search;
}
