package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentAddV1")
public class AttachmentAddDTOv1 extends AttachmentBaseDTOv1 {

    @Schema(name = "twinClassFieldId", description = "link to the field to which attachment was added (if any)")
    public UUID twinClassFieldId;
    @Schema(description = "link to the comment to which attachment was added (if any)")
    public UUID commentId;

}
