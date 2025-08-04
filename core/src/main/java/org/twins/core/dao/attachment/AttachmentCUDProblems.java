package org.twins.core.dao.attachment;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttachmentCUDProblems extends AttachmentCreateProblems {
    private List<AttachmentUpdateProblem> updateProblems = new ArrayList<>();
    private List<AttachmentDeleteProblem> deleteProblems = new ArrayList<>();

    public boolean hasProblems() {
        return super.hasProblems() ||
                !updateProblems.isEmpty() ||
                !deleteProblems.isEmpty();
    }
}
