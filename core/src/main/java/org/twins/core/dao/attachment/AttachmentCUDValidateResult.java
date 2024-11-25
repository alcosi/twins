package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class AttachmentCUDValidateResult {
    public Set<AttachmentProblem> twinAttachmentProblems;
    public Map<UUID, Set<AttachmentProblem>> twinFieldAttachmentProblems;
    public Map<UUID, Set<AttachmentProblem>> twinCommentAttachmentProblems;
    public List<TwinAttachmentEntity> attachmentsForUpdateDeleteOperations;
}
