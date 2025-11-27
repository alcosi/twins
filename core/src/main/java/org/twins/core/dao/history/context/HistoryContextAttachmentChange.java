package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class HistoryContextAttachmentChange extends HistoryContextAttachment {
    public static final String DISCRIMINATOR = "history.attachmentChange";
    private String newStorageFileKey;
    private String newExternalId;
    private String newTitle;
    private String newDescription;
    private Integer newOrder;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        if (newStorageFileKey != null)
            vars.put("attachment.newStorageFileKey", newStorageFileKey);
        if (newExternalId != null)
            vars.put("attachment.newExternalId", newExternalId);
        if (newTitle != null)
            vars.put("attachment.newTitle", newTitle);
        if (newDescription != null)
            vars.put("attachment.newDescription", newDescription);
        if (newOrder != null)
            vars.put("attachment.newOrder", String.valueOf(newOrder));
        return vars;
    }
}
