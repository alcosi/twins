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
@Schema(name =  "AttachmentCreateValidateRsV1")
public class AttachmentCreateValidateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "Twin attachment problems")
    public AttachmentCUDProblemsDTOv1 cudProblems;
}
