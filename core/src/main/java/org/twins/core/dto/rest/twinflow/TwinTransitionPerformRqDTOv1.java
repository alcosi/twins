package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.dto.rest.twin.TwinUpdateRqDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformRqV1")
public class TwinTransitionPerformRqDTOv1 extends Request {
    @Schema(description = "Target twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema
    public String comment;

    @Schema(description = "Data to be update in target twin during transition (if allowEdit = true)")
    public TwinUpdateDTOv1 twinUpdate;
}
