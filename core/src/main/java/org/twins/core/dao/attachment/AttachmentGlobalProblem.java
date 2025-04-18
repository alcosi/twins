package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AttachmentGlobalProblem extends AttachmentProblem {
    private AttachmentGlobalCreateDeleteProblem problem;
}
