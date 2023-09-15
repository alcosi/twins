package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentViewRsV1")
public class AttachmentViewRsDTOv1 extends Response {
    @Schema(description = "attachment details")
    public AttachmentDTOv1 attachment;
}
