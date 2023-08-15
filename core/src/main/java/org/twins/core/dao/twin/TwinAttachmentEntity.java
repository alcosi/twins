package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_attachment")
public class TwinAttachmentEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "storage_link")
    private String storageLink;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

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
}
