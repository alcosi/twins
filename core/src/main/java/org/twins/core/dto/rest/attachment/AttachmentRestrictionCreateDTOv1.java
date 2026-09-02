package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AttachmentRestrictionCreateV1")
public class AttachmentRestrictionCreateDTOv1 extends AttachmentRestrictionSaveDTOv1 {
}
