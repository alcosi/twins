package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "SchedulerListRsV1")
public class SchedulerListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - scheduler list")
    public List<SchedulerDTOv1> schedulers;
}
