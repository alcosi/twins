package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinflowFactorySearchRqV1")
public class TwinflowFactorySearchRqDTOv1 extends Request {

    @Schema(description = "search DTO")
    private TwinflowFactorySearchDTOv1 search;
}
