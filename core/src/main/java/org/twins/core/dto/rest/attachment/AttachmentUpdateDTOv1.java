package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentUpdateV1")
public class AttachmentUpdateDTOv1 extends AttachmentSaveDTOv1 {
    @Schema(description = "id", example = DTOExamples.ATTACHMENT_ID)
    public UUID id;
}
