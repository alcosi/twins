package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.HashMap;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class HistoryContextAttachment extends HistoryContext {
    public static final String DISCRIMINATOR = "history.attachment";
    private AttachmentSnapshot attachment;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("attachment.id", attachment != null ? attachment.id.toString() : "");
        vars.put("attachment.storageLink", attachment != null ? attachment.storageLink : "");
        vars.put("attachment.externalId", attachment != null ? attachment.externalId : "");
        vars.put("attachment.title", attachment != null ? attachment.title : "");
        vars.put("attachment.description", attachment != null ? attachment.description : "");
        return vars;
    }

    public HistoryContextAttachment shotAttachment(TwinAttachmentEntity attachmentEntity) {
        attachment = AttachmentSnapshot.convertEntity(attachmentEntity);
        return this;
    }

    @Data
    @Accessors(chain = true)
    public static final class AttachmentSnapshot {
        private UUID id;
        private String storageLink;
        private String externalId;
        private String title;
        private String description;

        public static AttachmentSnapshot convertEntity(TwinAttachmentEntity attachmentEntity) {
            if (attachmentEntity == null)
                return null;
            return new AttachmentSnapshot()
                    .setId(attachmentEntity.getId())
                    .setStorageLink(attachmentEntity.getStorageLink())
                    .setExternalId(attachmentEntity.getExternalId())
                    .setTitle(attachmentEntity.getTitle())
                    .setDescription(attachmentEntity.getDescription());
        }
    }
}
