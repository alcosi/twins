package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.i18n.dao.I18nEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;

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

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "src_twin_status_id")
    private UUID srcTwinStatusId;

    @Column(name = "dst_twin_status_id")
    private UUID dstTwinStatusId;

    @Column(name = "screen_id")
    private UUID screenId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
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
    private UUID twinflowTransitionAliasId;

    @ManyToOne
    @JoinColumn(name = "twinflow_transition_alias_id", insertable = false, updatable = false, nullable = false)
    private TwinflowTransitionAliasEntity twinflowTransitionAlias;

    @ManyToOne
    @JoinColumn(name = "twinflow_id", insertable = false, updatable = false, nullable = false)
    private TwinflowEntity twinflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    private I18nEntity descriptionI18n;

    @ManyToOne
    @JoinColumn(name = "src_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity srcTwinStatus;

    @ManyToOne
    @JoinColumn(name = "dst_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity dstTwinStatus;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionValidatorRuleEntity, UUID> validatorRulesKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionTriggerEntity, UUID> triggersKit;

    @Transient // because field can be useful only in admin panel
    @EqualsAndHashCode.Exclude
    private PermissionEntity permission;

    @Transient // because field can be useful only in admin panel
    @EqualsAndHashCode.Exclude
    private UserEntity createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbuilt_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity inbuiltFactory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drafting_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity draftingFactory;

    @Override
    public String easyLog(Level level) {
        return "twinflowTransition[id:" + id + "]";
    }
}
