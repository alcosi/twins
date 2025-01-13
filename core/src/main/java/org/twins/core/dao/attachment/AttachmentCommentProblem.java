package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.UUID;

@Data
public class AttachmentCommentProblem extends AttachmentProblem {

    private UUID commentId;
    private CommentAttachmentProblem problem;

}
