package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTransitionTriggerUpdateRqV1")
public class TwinStatusTransitionTriggerUpdateRqDTOv1 extends Request {
    @Schema(description = "twin status transition triggers")
    public List<TwinStatusTransitionTriggerUpdateDTOv1> twinStatusTransitionTriggers;
}
