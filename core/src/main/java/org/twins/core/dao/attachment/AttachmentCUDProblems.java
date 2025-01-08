package org.twins.core.dao.attachment;

import lombok.Data;
import org.twins.core.dto.rest.attachment.*;

import java.util.List;

@Data
public class AttachmentCUDProblems {
    private List<AttachmentCreateProblem> createProblems;
    private List<AttachmentUpdateProblem> updateProblems;
    private List<AttachmentDeleteProblem> deleteProblems;
    private List<TwinFieldAttachmentProblemsDTOv1> fieldAttachmentProblems;
    private List<TwinCommentAttachmentProblemsDTOv1> commentAttachmentProblems;
}
