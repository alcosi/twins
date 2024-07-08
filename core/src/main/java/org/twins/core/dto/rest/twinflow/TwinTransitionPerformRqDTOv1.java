package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformRqV1")
public class TwinTransitionPerformRqDTOv1 extends TwinTransitionDraftRqDTOv1 {
    @Schema
    public String comment;

    @Schema(description = "some extra data to perform transition")
    public TwinTransitionContextDTOv1 context;
}
