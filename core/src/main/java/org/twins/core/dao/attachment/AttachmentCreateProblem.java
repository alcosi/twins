package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.attachment.AttachmentFileCreateUpdateProblem;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AttachmentCreateProblem extends AttachmentProblem {
    private String id;
    private AttachmentFileCreateUpdateProblem problem;
}
