package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentCUDProblemsDTOv1")
public class AttachmentCUDProblemsDTOv1 {

    @Schema(description = "create problems")
    public List<AttachmentFileCreateProblemDTOv1> createProblems;
    @Schema(description = "update problems")
    public List<AttachmentFileUpdateProblemDTOv1> updateProblems;
    @Schema(description = "delete problems")
    public List<AttachmentFileDeleteProblemDTOv1> deleteProblems;
    @Schema(description = "field attachment problems")
    public List<TwinFieldAttachmentProblemsDTOv1> fieldAttachmentProblems;
    @Schema(description = "comment attachment problems")
    public List<TwinCommentAttachmentProblemsDTOv1> commentAttachmentProblems;

}
