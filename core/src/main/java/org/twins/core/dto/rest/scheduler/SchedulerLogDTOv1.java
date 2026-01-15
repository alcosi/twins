package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SchedulerLogV1")
public class SchedulerLogDTOv1 {

    @Schema(example = DTOExamples.UUID_ID, description = "id")
    public UUID id;

    @Schema(example = DTOExamples.UUID_ID, description = "scheduler id")
    @RelatedObject(type = SchedulerDTOv1.class, name = "scheduler")
    public UUID schedulerId;

    @Schema(example = DTOExamples.INSTANT, description = "created at")
    public LocalDateTime createdAt;

    @Schema(example = DTOExamples.DESCRIPTION, description = "scheduler log result")
    public String result;

    @Schema(example = DTOExamples.TIME_IN_MILLIS, description = "execution time of scheduler task")
    public Long executionTime;
}
