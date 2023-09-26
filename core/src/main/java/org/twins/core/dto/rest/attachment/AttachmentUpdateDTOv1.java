package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentUpdateV1")
public class AttachmentUpdateDTOv1 extends AttachmentBaseDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
