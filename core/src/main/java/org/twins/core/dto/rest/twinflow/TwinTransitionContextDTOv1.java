package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.link.TwinLinkCudDTOv1;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinTransitionContextV1")
public class TwinTransitionContextDTOv1 {
    @Schema(description = "fields")
    public Map<UUID, String> fields; // uuids, but not strings as key. because fields from different twin_classes can be passed here

    @Schema(description = "Attachments for create/update/delete")
    public AttachmentCudDTOv1 attachments;

    @Schema(description = "TwinLinks for create/update/delete")
    public TwinLinkCudDTOv1 twinLinks;

    @Schema(description = "list of twins, that must be created during transition")
    public List<TwinCreateRqDTOv2> newTwins;
}
