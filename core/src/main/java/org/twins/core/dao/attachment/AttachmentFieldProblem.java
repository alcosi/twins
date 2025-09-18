package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.attachment.FieldAttachmentProblem;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AttachmentFieldProblem extends AttachmentProblem {

    private UUID twinFieldId;
    private FieldAttachmentProblem problem;

}
