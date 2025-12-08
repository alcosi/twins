package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SchedulerV1")
public class SchedulerDTOv1 {

    @Schema(example = DTOExamples.FACE_ID)
    private UUID id;

    @Schema(example = DTOExamples.FEATURER_ID)
    @RelatedObject(type = FeaturerEntity.class, name = "featurer")
    private int featurerId;

    @Schema(example = DTOExamples.FEATURER_PARAM)
    private Map<String, String> schedulerParams;

    @Schema(example = DTOExamples.BOOLEAN_TRUE)
    private boolean active;

    @Schema(example = DTOExamples.BOOLEAN_TRUE)
    private boolean logEnabled;

    @Schema(example = DTOExamples.SCHEDULER_CRON)
    private String cron;

    @Schema(example = DTOExamples.SCHEDULER_TIME_IN_MILLIS)
    private Integer fixedRate;

    @Schema(example = DTOExamples.DESCRIPTION)
    private String description;

    @Schema(example = DTOExamples.INSTANT)
    private Timestamp createdAt;

    @Schema(example = DTOExamples.INSTANT)
    private Timestamp updatedAt;
}
