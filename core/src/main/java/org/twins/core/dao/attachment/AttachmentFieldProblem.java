package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.UUID;

@Data
public class AttachmentFieldProblem extends AttachmentProblem {

    private UUID twinFieldId;
    private FieldAttachmentProblem problem;

}
