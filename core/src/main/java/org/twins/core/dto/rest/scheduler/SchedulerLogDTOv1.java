package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SchedulerV1")
public class SchedulerLogDTOv1 {

    @Schema(example = DTOExamples.FACE_ID)
    private UUID id;

    @Schema(example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = SchedulerEntity.class, name = "scheduler")
    private UUID schedulerId;

    @Schema(example = DTOExamples.INSTANT)
    private Timestamp createdAt;

    @Schema(example = DTOExamples.DESCRIPTION)
    private String result;

    @Schema(example = DTOExamples.SCHEDULER_TIME_IN_MILLIS)
    private long executionTime;
}
