package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowViewRsV1")
public class TwinflowViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twinflow details")
    public TwinflowBaseDTOv1 twinflow;
}
