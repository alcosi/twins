package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryContextAttachment extends HistoryContext {
    public static final String DISCRIMINATOR = "history.attachment";
    private UUID attachmentId;
    private AttachmentDraft attachment;

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
        vars.put("attachment.storageLink", attachment != null ? attachment.storageLink : "");
        vars.put("attachment.externalId", attachment != null ? attachment.externalId : "");
        vars.put("attachment.title", attachment != null ? attachment.title : "");
        vars.put("attachment.description", attachment != null ? attachment.description : "");
        return vars;
    }

    @Data
    @Accessors(chain = true)
    public static final class AttachmentDraft {
        private String storageLink;
        private String externalId;
        private String title;
        private String description;

        public static AttachmentDraft convertEntity(TwinAttachmentEntity attachmentEntity) {
            if (attachmentEntity == null)
                return null;
            return new AttachmentDraft()
                    .setStorageLink(attachmentEntity.getStorageLink())
                    .setExternalId(attachmentEntity.getExternalId())
                    .setTitle(attachmentEntity.getTitle())
                    .setDescription(attachmentEntity.getDescription());
        }
    }
}
