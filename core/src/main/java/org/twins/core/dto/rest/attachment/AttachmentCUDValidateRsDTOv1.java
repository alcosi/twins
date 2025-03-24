package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentCUDValidateRsV1")
public class AttachmentCUDValidateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "Twin attachment problems")
    public AttachmentCUDProblemsDTOv1 cudProblems;

    @Schema(description = "Attachment entities for update and delete operations")
    public List<AttachmentDTOv1> attachmentsForUD;
}
