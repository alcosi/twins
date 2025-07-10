package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttachmentCUDValidateResult extends AttachmentCreateValidateResult {
    public AttachmentCUDProblems cudProblems = new AttachmentCUDProblems();

    public boolean hasProblems() {
        return getCudProblems().hasProblems();
    }
}
