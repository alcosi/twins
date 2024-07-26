package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPrepBatchRsV1")
public class TwinTransitionPrepBatchRsDTOv1 extends Request {
    @Schema(description = "some extra data to prepare transition")
    public TwinTransitionContextDTOv1 draftContext;
}
