package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_attachment_delete_task")
@FieldNameConstants
public class AttachmentDeleteTaskEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_attachment_id")
    private UUID twinAttachmentId;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "twin_owner_business_account_id")
    private UUID twinOwnerBusinessAccountId;

    @Column(name = "twin_created_by_user_id")
    private UUID twinCreatedByUserId;

    @Column(name = "storage_id")
    private UUID storageId;

    @Column(name = "storage_file_key")
    private String storageFileKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttachmentDeleteTaskStatus status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private StorageEntity storage;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL -> "attachmentDeleteTask[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", storageId:" + storageId + ", status:" + status + "]";
            case DETAILED -> "attachmentDeleteTask[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", twinId:" + twinId + ", domainId:" + domainId + ", twinOwnerBusinessAccountId:" + twinOwnerBusinessAccountId + ", twinCreatedByUserId:" + twinCreatedByUserId + ", storageId:" + storageId + ", storageFileKey:" + storageFileKey + ", createdAt:" + createdAt + "]";
            default -> "attachmentDeleteTask[id:" + id + "]";
        };
    }
}
