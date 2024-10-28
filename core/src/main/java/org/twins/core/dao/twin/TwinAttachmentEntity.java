package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_attachment")
public class TwinAttachmentEntity implements PublicCloneable<TwinAttachmentEntity>, TwinFieldStorage, EasyLoggable {
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

    @Column(name = "storage_link")
    private String storageLink;

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

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "twin_comment_id")
    private UUID twinCommentId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

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

    @Transient
    @EqualsAndHashCode.Exclude
    private Set<TwinAttachmentAction> attachmentActions;

    @Override
    public TwinAttachmentEntity clone() {
        return new TwinAttachmentEntity()
                .setTwinId(twinId)
                .setTwin(twin)
                .setCreatedByUser(createdByUser)
                .setExternalId(externalId)
                .setTitle(title)
                .setDescription(description)
                .setStorageLink(storageLink)
                .setCreatedAt(createdAt)
                .setTwinCommentId(twinCommentId)
                .setTwinflowTransition(twinflowTransition)
                .setTwinflowTransitionId(twinflowTransitionId)
                .setViewPermission(viewPermission)
                .setViewPermissionId(viewPermissionId);
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "attachment[" + id + "]";
            case NORMAL -> "attachment[id:" + id + ", twinId:" + twinId + "]";
            default -> "attachment[id:" + id + ", twinId:" + twinId + ", storageLink:" + storageLink + "]";
        };

    }
}
