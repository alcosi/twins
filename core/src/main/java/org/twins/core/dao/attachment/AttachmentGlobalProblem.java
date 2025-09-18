package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.attachment.AttachmentGlobalCreateDeleteProblem;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AttachmentGlobalProblem extends AttachmentProblem {
    private AttachmentGlobalCreateDeleteProblem problem;
}
