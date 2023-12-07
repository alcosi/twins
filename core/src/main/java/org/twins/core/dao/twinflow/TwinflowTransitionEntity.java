package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition")
public class TwinflowTransitionEntity implements EasyLoggable {
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

    @Column(name = "allow_comment")
    private boolean allowComment;

    @Column(name = "allow_attachments")
    private boolean allowAttachment;

    @Column(name = "allow_links")
    private boolean allowLinks;

    @Column(name = "allow_edit")
    private boolean allowEdit;

    @Column(name = "inbuilt_twin_factory_id")
    private UUID inbuiltTwinFactoryId;

    @Column(name = "drafting_twin_factory_id")
    private UUID draftingTwinFactoryId;

    @ManyToOne
    @JoinColumn(name = "twinflow_id", insertable = false, updatable = false, nullable = false)
    private TwinflowEntity twinflow;

//    @ManyToOne
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;

    @ManyToOne
    @JoinColumn(name = "src_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity srcTwinStatus;

    @ManyToOne
    @JoinColumn(name = "dst_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity dstTwinStatus;

//    @ManyToOne
//    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
//    private PermissionEntity permission;

//    @ManyToOne
//    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
//    private UserEntity createdByUser;

    @Override
    public String easyLog(Level level) {
        return "twinflowTransition[id:" + id + "]";
    }
}
