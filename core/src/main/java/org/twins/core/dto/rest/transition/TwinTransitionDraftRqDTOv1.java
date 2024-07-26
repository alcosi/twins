package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionDraftRqV1")
public class TwinTransitionDraftRqDTOv1 extends TwinTransitionPrepRqDTOv1 {
    @Schema
    public String comment;

    @Schema(description = "some extra data to draft transition")
    public TwinTransitionContextDTOv1 context;
}
