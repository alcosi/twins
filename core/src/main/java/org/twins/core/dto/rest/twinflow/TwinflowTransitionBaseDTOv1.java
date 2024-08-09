package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinflowTransitionBaseV1")
public class TwinflowTransitionBaseDTOv1 {
    @Schema(example = DTOExamples.TWINFLOW_TRANSITION_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWIN_STATUS_ID)
    public UUID dstTwinStatusId;

    @Schema(description = "status")
    public TwinStatusDTOv1 dstTwinStatus;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "alias", example = DTOExamples.TWINFLOW_TRANSITION_ALIAS)
    public String alias;

    @Schema()
    public boolean allowComment;

    @Schema()
    public boolean allowAttachments;

    @Schema()
    public boolean allowLinks;
}
