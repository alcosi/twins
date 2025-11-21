package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
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

    @Column(name = "storage_id")
    private UUID storageId;

    @Column(name = "storage_file_key")
    private String storageFileKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttachmentDeleteTaskStatus status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne()
    @JoinColumn(name = "storage_id", insertable = false, updatable = false, nullable = false)
    private StorageEntity storage;

    @ManyToOne()
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL -> "attachmentDeleteTask[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", storageId:" + storageId + "]";
            case DETAILED ->
                    "attachmentDeleteTask[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", twinId:" + twinId + ", storageId:" + storageId + ", storageFileKey:" + storageFileKey + "]";
            default -> "attachmentDeleteTask[id:" + id + "]";
        };
    }
}
