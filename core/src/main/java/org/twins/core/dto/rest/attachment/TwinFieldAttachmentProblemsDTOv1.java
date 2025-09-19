package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.attachment.problem.FieldAttachmentProblem;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinFieldAttachmentProblemsV1")
public class TwinFieldAttachmentProblemsDTOv1 extends AttachmentProblemDTOv1 {

    @Schema(description = "Twin field id")
    public UUID twinFieldId;

    @Schema(description = "Twin field attachment problem")
    public FieldAttachmentProblem problem;

}
