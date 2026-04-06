package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinTriggerTaskSearchRqV1")
public class TwinTriggerTaskSearchRqDTOv1 extends Request {
    @Schema
    public TwinTriggerTaskSearchDTOv1 search;
}
