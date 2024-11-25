package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "AttachmentCUDValidateRqV1")
public class AttachmentCUDValidateRqDTOv1 {
    @Schema(description = "Twin id")
    public UUID twinId;

    @Schema(description = "Attachments for validation")
    public AttachmentCudDTOv1 attachments;
}
