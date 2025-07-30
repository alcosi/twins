package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentCreateProblemsV1")
public class AttachmentCreateProblemsDTOv1 {
    @Schema(description = "create problems")
    public List<AttachmentFileCreateProblemDTOv1> createProblems;
    @Schema(description = "field attachment problems")
    public List<TwinFieldAttachmentProblemsDTOv1> fieldAttachmentProblems;
    @Schema(description = "comment attachment problems")
    public List<TwinCommentAttachmentProblemsDTOv1> commentAttachmentProblems;
}
