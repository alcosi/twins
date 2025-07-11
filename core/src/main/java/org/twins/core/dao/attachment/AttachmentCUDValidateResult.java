package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttachmentCUDValidateResult extends AttachmentCreateValidateResult {
    public AttachmentCUDProblems cudProblems = new AttachmentCUDProblems();
    public List<TwinAttachmentEntity> attachmentsForUD = new ArrayList<>();

    public boolean hasProblems() {
        return getCudProblems().hasProblems();
    }
}
