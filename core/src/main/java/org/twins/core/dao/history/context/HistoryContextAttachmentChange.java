package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class HistoryContextAttachmentChange extends HistoryContextAttachment {
    public static final String DISCRIMINATOR = "history.attachmentChange";
    private String newStorageLink;
    private String newExternalId;
    private String newTitle;
    private String newDescription;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        if (newStorageLink != null)
            vars.put("attachment.newStorageLink", newStorageLink);
        if (newExternalId != null)
            vars.put("attachment.newExternalId", newExternalId);
        if (newTitle != null)
            vars.put("attachment.newTitle", newTitle);
        if (newDescription != null)
            vars.put("attachment.newDescription", newDescription);
        return vars;
    }
}
