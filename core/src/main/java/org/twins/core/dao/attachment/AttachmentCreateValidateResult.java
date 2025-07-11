package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttachmentCreateValidateResult {
    public AttachmentCreateProblems createProblems = new AttachmentCreateProblems();

    public boolean hasProblems() {
        return getCreateProblems().hasProblems();
    }
}
