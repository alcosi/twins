package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class AttachmentSnapshot {
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

    public static void extractTemplateVars(HashMap<String, String> vars, AttachmentSnapshot attachmentSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", attachmentSnapshot != null ? attachmentSnapshot.id.toString() : "");
        vars.put(prefix + "storageLink", attachmentSnapshot != null ? attachmentSnapshot.storageLink : "");
        vars.put(prefix + "externalId", attachmentSnapshot != null ? attachmentSnapshot.externalId : "");
        vars.put(prefix + "title", attachmentSnapshot != null ? attachmentSnapshot.title : "");
        vars.put(prefix + "description", attachmentSnapshot != null ? attachmentSnapshot.description : "");
    }
}
