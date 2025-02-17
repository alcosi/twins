package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinDTOv1;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "AttachmentV2")
public class AttachmentDTOv2 extends AttachmentDTOv1 {
    @Schema(description = "twin")
    public TwinDTOv1 twin;
}
