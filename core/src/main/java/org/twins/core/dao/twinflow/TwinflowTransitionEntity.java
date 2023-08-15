package org.twins.core.dao.twinflow;

import lombok.Data;

import jakarta.persistence.*;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.permission.PermissionEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinflow_transition")
public class TwinflowTransitionEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_id")
    private UUID twinflowId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "src_twin_status_id")
    private UUID srcTwinStatusId;

    @Column(name = "dst_twin_status_id")
    private UUID dstTwinStatusId;

    @Column(name = "screen_id")
    private UUID screenId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @ManyToOne
    @JoinColumn(name = "twinflow_id", insertable = false, updatable = false, nullable = false)
    private TwinflowEntity twinflow;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByNameI18NId;

    @ManyToOne
    @JoinColumn(name = "src_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity twinStatusBySrcTwinStatusId;

    @ManyToOne
    @JoinColumn(name = "dst_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity twinStatusByDstTwinStatusId;

    @ManyToOne
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private PermissionEntity permission;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
