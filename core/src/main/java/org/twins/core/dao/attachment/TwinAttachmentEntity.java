package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.file.DomainFile;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_attachment")
public class TwinAttachmentEntity implements PublicCloneable<TwinAttachmentEntity>, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "storage_file_key")
    private String storageFileKey;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "size")
    private Long size;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "twin_comment_id")
    private UUID twinCommentId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;


    @Column(name = "storage_id", nullable = false)
    private UUID storageId;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twinflow_transition_id", insertable = false, updatable = false)
    private TwinflowTransitionEntity twinflowTransition;

    @ManyToOne
    @JoinColumn(name = "view_permission_id", insertable = false, updatable = false)
    private PermissionEntity viewPermission;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name = "twin_comment_id", insertable = false, updatable = false)
    private TwinCommentEntity comment;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassField;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_id", insertable = false, updatable = false)
    private StorageEntity storage;

    @Transient
    @EqualsAndHashCode.Exclude
    private Set<TwinAttachmentAction> attachmentActions;

    @Transient
    @EqualsAndHashCode.Exclude
    private DomainFile attachmentFile;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinAttachmentModificationEntity, String> modifications;

    @Override
    public TwinAttachmentEntity clone() {
        return new TwinAttachmentEntity()
                .setTwinId(twinId)
                .setTwin(twin)
                .setCreatedByUser(createdByUser)
                .setCreatedByUserId(createdByUserId)
                .setExternalId(externalId)
                .setTitle(title)
                .setDescription(description)
                .setStorageFileKey(storageFileKey)
                .setModifications(modifications)
                .setCreatedAt(createdAt)
                .setTwinCommentId(twinCommentId)
                .setTwinflowTransition(twinflowTransition)
                .setTwinflowTransitionId(twinflowTransitionId)
                .setViewPermission(viewPermission)
                .setViewPermissionId(viewPermissionId)
                .setSize(size);
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "attachment[" + id + "]";
            case NORMAL -> "attachment[id:" + id + ", twinId:" + twinId + "]";
            default -> "attachment[id:" + id + ", twinId:" + twinId + ", storageLinks:" + storageFileKey + "]";
        };

    }
}
