package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.TaskStatus;
import org.twins.core.dao.resource.StorageEntity;

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

    @Column(name = "storage_id")
    private UUID storageId;

    @Column(name = "storage_file_key")
    private String storageFileKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false, updatable = false, nullable = false)
    private StorageEntity storage;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL -> "attachmentDeleteTask[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", storageId:" + storageId + "]";
            case DETAILED ->
                    "attachmentDeleteTask[id:" + id + ", twinAttachmentId:" + twinAttachmentId + ", storageId:" + storageId + ", storageFileKey:" + storageFileKey + "]";
            default -> "attachmentDeleteTask[id:" + id + "]";
        };
    }
}
