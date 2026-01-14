package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_attachment_modification", uniqueConstraints = @UniqueConstraint(
        name = "UK_twin_attachment_modification",
        columnNames = {"twin_attachment_id", "modification_type"}
))
public class TwinAttachmentModificationEntity implements PublicCloneable<TwinAttachmentModificationEntity>, EasyLoggable {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_attachment_id")
    private UUID twinAttachmentId;

    @Column(name = "modification_type")
    private String modificationType;

    @Column(name = "storage_file_key")
    private String storageFileKey;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinAttachmentEntity twinAttachment;

    @Override
    public TwinAttachmentModificationEntity clone() {
        return new TwinAttachmentModificationEntity()
                .setTwinAttachmentId(twinAttachmentId)
                .setModificationType(modificationType)
                .setStorageFileKey(storageFileKey);
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "attachmentModification[" + id + "]";
            case NORMAL -> "attachmentModification[id:" + id + ", twinAttachmentId:" + twinAttachmentId + "]";
            default -> "attachmentModification[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", modificationType:" + modificationType + ", storageFileKey:" + storageFileKey + "]";
        };

    }
}
