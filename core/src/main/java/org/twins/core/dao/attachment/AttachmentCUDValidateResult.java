package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentCUDValidateResult {
    public AttachmentCUDProblems cudProblems;
    public List<TwinAttachmentEntity> attachmentsForUD;
}
