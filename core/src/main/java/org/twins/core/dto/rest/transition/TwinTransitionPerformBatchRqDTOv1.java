package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformBatchRqV1")
public class TwinTransitionPerformBatchRqDTOv1 extends TwinTransitionPrepBatchRqDTOv1 {
    @Schema
    public String batchComment;

    @Schema(description = "some extra data to perform transition")
    public TwinTransitionContextDTOv1 batchContext;
}
