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
@Schema(name = "AttachmentRestrictionUpdateV1")
public class AttachmentRestrictionUpdateDTOv1 extends AttachmentRestrictionSaveDTOv1 {
    @Schema(description = "attachment restriction id", example = DTOExamples.UUID_ID)
    public UUID id;
}
