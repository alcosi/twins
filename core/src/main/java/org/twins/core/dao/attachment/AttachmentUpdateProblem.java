package org.twins.core.dao.attachment;

import lombok.Data;

@Data
public class AttachmentUpdateProblem extends AttachmentProblem {

    private String externalId;
    private AttachmentFileCreateUpdateProblem problem;

}
