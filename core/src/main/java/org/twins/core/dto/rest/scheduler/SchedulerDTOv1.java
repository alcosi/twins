package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SchedulerV1")
public class SchedulerDTOv1 {

    @Schema(example = DTOExamples.UUID_ID, description = "id")
    public UUID id;

    @Schema(example = DTOExamples.UUID_ID, description = "domain id")
    public UUID domainId;

    @Schema(example = DTOExamples.FEATURER_ID, description = "scheduler featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "schedulerFeaturer")
    public Integer schedulerFeaturerId;

    @Schema(example = DTOExamples.FEATURER_PARAM, description = "params for scheduler featurer")
    public Map<String, String> schedulerParams;

    @Schema(example = DTOExamples.BOOLEAN_TRUE, description = "flag to activate/deactivate scheduler")
    public Boolean active;

    @Schema(example = DTOExamples.BOOLEAN_TRUE, description = "flag to enable/disable logging scheduler results to db")
    public Boolean logEnabled;

    @Schema(example = DTOExamples.CRON, description = "time condition to run scheduler task")
    public String cron;

    @Schema(example = DTOExamples.TIME_IN_MILLIS, description = "time interval in ms at which scheduler will run task")
    public Integer fixedRate;

    @Schema(example = DTOExamples.DESCRIPTION, description = "description")
    public String description;

    @Schema(example = DTOExamples.INSTANT, description = "created at")
    public LocalDateTime createdAt;

    @Schema(example = DTOExamples.INSTANT, description = "updated at")
    public LocalDateTime updatedAt;
}
