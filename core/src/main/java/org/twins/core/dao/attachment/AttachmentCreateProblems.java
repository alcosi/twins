package org.twins.core.dao.attachment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttachmentCreateProblems {
    private List<AttachmentCreateProblem> createProblems = new ArrayList<>();
    private List<AttachmentGlobalProblem> globalProblems = new ArrayList<>();
    private List<AttachmentFieldProblem> fieldAttachmentProblems = new ArrayList<>();
    private List<AttachmentCommentProblem> commentAttachmentProblems = new ArrayList<>();

    public boolean hasProblems() {
        return !createProblems.isEmpty() ||
                !fieldAttachmentProblems.isEmpty() ||
                !commentAttachmentProblems.isEmpty() ||
                !globalProblems.isEmpty();
    }
}
