package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TransitionTriggerSearchRqV1")
public class TransitionTriggerSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public TransitionTriggerSearchDTOv1 search;
}
