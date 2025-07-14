package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "AttachmentCreateValidateRqV1")
public class AttachmentCreateValidateRqDTOv1 {
    @Schema(description = "Twin class id")
    public UUID twinClassId;

    @Schema(description = "Attachments for adding")
    public List<AttachmentCreateDTOv1> create;
}
