package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.common.file.FileData;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.attachment.TwinAttachmentAction;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_attachment")
public class TwinAttachmentEntity implements PublicCloneable<TwinAttachmentEntity>, EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
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

    @Column(name = "`order`")
    private Integer order;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity twinSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_transition_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowTransitionEntity twinflowTransitionSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "view_permission_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity viewPermissionSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_comment_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinCommentEntity commentSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity twinClassFieldSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private StorageEntity storageSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity twin;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowTransitionEntity twinflowTransition;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity viewPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinCommentEntity comment;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity twinClassField;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private StorageEntity storage;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<TwinAttachmentAction> attachmentActions;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FileData attachmentFile;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private boolean fileChanged = false;

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
                .setStorageId(storageId)
                .setStorage(storage)
                .setStorageFileKey(storageFileKey)
                .setFileChanged(fileChanged)
                .setModifications(modifications)
                .setCreatedAt(createdAt)
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassFieldId)
                .setTwinCommentId(twinCommentId)
                .setComment(comment)
                .setTwinflowTransition(twinflowTransition)
                .setTwinflowTransitionId(twinflowTransitionId)
                .setViewPermission(viewPermission)
                .setViewPermissionId(viewPermissionId)
                .setSize(size)
                .setOrder(order);
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "attachment[" + id + "]";
            case NORMAL -> "attachment[id:" + id + ", twinId:" + twinId + "]";
            default -> "attachment[id:" + id + ", twinId:" + twinId + ", storageLinks:" + storageFileKey + "]";
        };

    }
}
