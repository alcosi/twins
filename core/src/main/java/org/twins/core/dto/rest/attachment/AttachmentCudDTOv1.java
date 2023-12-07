package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "AttachmentCudV1")
public class AttachmentCudDTOv1 {
    @Schema(description = "Attachments for adding")
    public List<AttachmentAddDTOv1> create;

    @Schema(description = "Attachments for updating")
    public List<AttachmentUpdateDTOv1> update;

    @Schema(description = "Attachments id list for deleting")
    public List<UUID> delete;
}
