package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.draft.DraftDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionDraftRsV1")
public class TwinTransitionDraftRsDTOv1 extends Response {
    @Schema(description = "transition draft. It can be browsed and commited")
    private DraftDTOv1 transitionDraft;
}
