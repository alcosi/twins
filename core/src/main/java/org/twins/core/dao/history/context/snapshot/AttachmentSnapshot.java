package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class AttachmentSnapshot {
    private UUID id;
    private String storageFileKey;
    private Map<String, String> modifications;
    private String externalId;
    private String title;
    private String description;

    public static AttachmentSnapshot convertEntity(TwinAttachmentEntity attachmentEntity) {
        if (attachmentEntity == null)
            return null;
        return new AttachmentSnapshot()
                .setId(attachmentEntity.getId())
                .setStorageFileKey(attachmentEntity.getStorageFileKey())
                .setModifications(convertModificationsToMap(attachmentEntity.getModifications()))
                .setExternalId(attachmentEntity.getExternalId())
                .setTitle(attachmentEntity.getTitle())
                .setDescription(attachmentEntity.getDescription());
    }

    private static Map<String, String> convertModificationsToMap(Set<TwinAttachmentModificationEntity> modifications) {
        Map<String, String> mods = new HashMap<>();
        if (!CollectionUtils.isEmpty(modifications)) {
            for (TwinAttachmentModificationEntity mod : modifications)
                mods.put(mod.getModificationType(), mod.getStorageFileKey());
        }
        return mods;
    }

    public static void extractTemplateVars(HashMap<String, String> vars, AttachmentSnapshot attachmentSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", attachmentSnapshot != null ? attachmentSnapshot.id.toString() : "");
        vars.put(prefix + "storageFileKey", attachmentSnapshot != null ? attachmentSnapshot.storageFileKey : "");
        vars.put(prefix + "modifications", attachmentSnapshot != null ? attachmentSnapshot.modifications.toString() : "");
        vars.put(prefix + "externalId", attachmentSnapshot != null ? attachmentSnapshot.externalId : "");
        vars.put(prefix + "title", attachmentSnapshot != null ? attachmentSnapshot.title : "");
        vars.put(prefix + "description", attachmentSnapshot != null ? attachmentSnapshot.description : "");
    }
}
