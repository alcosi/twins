package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttachmentCUDValidateResult {
    public AttachmentCUDProblems cudProblems = new AttachmentCUDProblems();
    public List<TwinAttachmentEntity> attachmentsForUD = new ArrayList<>();
}
