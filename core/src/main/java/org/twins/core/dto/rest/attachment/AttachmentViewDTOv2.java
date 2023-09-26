package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "AttachmentViewV2")
public class AttachmentViewDTOv2 extends AttachmentViewDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID twinId;
}
