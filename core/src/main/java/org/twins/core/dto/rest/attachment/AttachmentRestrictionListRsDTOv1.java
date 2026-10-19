package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentRestrictionListRsV1")
public class AttachmentRestrictionListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "attachment restriction list")
    public List<AttachmentRestrictionDTOv1> attachmentRestrictions;
}
