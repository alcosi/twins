package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerSearchRqV1")
public class TwinStatusTriggerSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public TwinStatusTriggerSearchDTOv1 search;
}
