package org.twins.core.dao.attachment;

import lombok.Data;

@Data
public class AttachmentCreateProblem extends AttachmentProblem {
    private String externalId;
    private AttachmentFileCreateUpdateProblem problem;
}
