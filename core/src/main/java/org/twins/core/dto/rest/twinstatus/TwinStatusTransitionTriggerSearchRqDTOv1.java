package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTransitionTriggerSearchRqV1")
public class TwinStatusTransitionTriggerSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public TwinStatusTransitionTriggerSearchDTOv1 search;
}
