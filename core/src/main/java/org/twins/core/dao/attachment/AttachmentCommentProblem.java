package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.attachment.problem.CommentAttachmentProblem;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AttachmentCommentProblem extends AttachmentProblem {

    private UUID commentId;
    private CommentAttachmentProblem problem;

}
