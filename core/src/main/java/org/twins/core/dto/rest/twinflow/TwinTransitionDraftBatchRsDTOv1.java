package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionDraftBatchRsV1")
public class TwinTransitionDraftBatchRsDTOv1 extends Request {
    @Schema(description = "some extra data draft to perform transition")
    public TwinTransitionContextDTOv1 draftContext;
}
