package org.twins.core.dao.attachment;

import lombok.Data;

@Data
public class AttachmentDeleteProblem extends AttachmentProblem {

    private String externalId;
    private AttachmentFileDeleteProblem problem;

}
