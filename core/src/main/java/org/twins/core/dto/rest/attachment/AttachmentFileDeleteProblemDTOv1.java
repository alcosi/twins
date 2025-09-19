package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.attachment.problem.AttachmentFileDeleteProblem;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentFileDeleteProblemsDTOv1")
public class AttachmentFileDeleteProblemDTOv1 extends AttachmentProblemDTOv1 {

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "attachment file delete problems")
    public AttachmentFileDeleteProblem problem;

}
