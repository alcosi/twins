package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerSearchRqV1")
public class TwinFactoryTriggerSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public TwinFactoryTriggerSearchDTOv1 search;
}
