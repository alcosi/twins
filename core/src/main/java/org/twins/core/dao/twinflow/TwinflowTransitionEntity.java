package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition")
@FieldNameConstants
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

    //todo delete it
    @Column(name = "allow_comment")
    private boolean allowComment;

    //todo delete it
    @Column(name = "allow_attachments")
    private boolean allowAttachment;

    //todo delete it
    @Column(name = "allow_links")
    private boolean allowLinks;

    @Column(name = "inbuilt_twin_factory_id")
    private UUID inbuiltTwinFactoryId;

    @Column(name = "drafting_twin_factory_id")
    private UUID draftingTwinFactoryId;

    @Column(name = "twinflow_transition_alias_id")
    private String twinflowTransitionAliasId;

    @ManyToOne
    @JoinColumn(name = "twinflow_id", insertable = false, updatable = false, nullable = false)
    private TwinflowEntity twinflow;

//    @ManyToOne
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;

    @ManyToOne
    @JoinColumn(name = "src_twin_status_id", insertable = false, updatable = false)
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
