package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AttachmentDeleteProblem extends AttachmentProblem {

    private String id;
    private AttachmentFileDeleteProblem problem;

}
