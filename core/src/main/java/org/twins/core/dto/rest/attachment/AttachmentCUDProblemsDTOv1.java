package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "AttachmentCUDProblemsDTOv1")
public class AttachmentCUDProblemsDTOv1 extends AttachmentCreateProblemsDTOv1 {
    @Schema(description = "update problems")
    public List<AttachmentFileUpdateProblemDTOv1> updateProblems;
    @Schema(description = "delete problems")
    public List<AttachmentFileDeleteProblemDTOv1> deleteProblems;
}
