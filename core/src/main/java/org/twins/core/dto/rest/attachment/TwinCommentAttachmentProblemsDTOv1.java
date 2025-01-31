package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.attachment.CommentAttachmentProblem;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinCommentAttachmentProblemsV1")
public class TwinCommentAttachmentProblemsDTOv1 extends AttachmentProblemDTOv1 {

    @Schema(description = "Comment id")
    public UUID commentId;

    @Schema(description = "Twin comment attachment problem")
    public CommentAttachmentProblem problem;

}
