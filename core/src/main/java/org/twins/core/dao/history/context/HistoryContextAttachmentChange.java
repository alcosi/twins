package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryContextAttachmentChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.attachmentChange";
    private UUID attachmentId;
    private HistoryContextAttachment.AttachmentDraft fromAttachment; // attachment draft with old data (before change)
    private String toStorageLink;
    private String toExternalId;
    private String toTitle;
    private String toDescription;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("attachment.id", attachmentId != null ? attachmentId.toString() : "");
        vars.put("attachment.fromStorageLink", fromAttachment != null ? fromAttachment.getStorageLink() : "");
        vars.put("attachment.fromExternalId", fromAttachment != null ? fromAttachment.getExternalId() : "");
        vars.put("attachment.fromTitle", fromAttachment != null ? fromAttachment.getTitle() : "");
        vars.put("attachment.fromDescription", fromAttachment != null ? fromAttachment.getDescription() : "");
        if (toStorageLink != null)
            vars.put("attachment.toStorageLink", toStorageLink);
        if (toExternalId != null)
            vars.put("attachment.toExternalId", toExternalId);
        if (toTitle != null)
            vars.put("attachment.toTitle", toTitle);
        if (toDescription != null)
            vars.put("attachment.toDescription", toDescription);
        return vars;
    }
}
