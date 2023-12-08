package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowTransitionViewV1")
public class TwinTransitionViewDTOv1 extends TwinTransitionBaseDTOv1 {
    @Schema(example = DTOExamples.TWIN_STATUS_ID)
    public UUID dstTwinStatusId;

    @Schema(description = "status")
    public TwinStatusDTOv1 dstTwinStatus;

    @Schema(description = "name")
    public String name;

    @Schema()
    public boolean allowComment;

    @Schema()
    public boolean allowAttachments;

    @Schema()
    public boolean allowLinks;
}
