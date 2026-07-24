package org.twins.core.service.permission;

import lombok.Getter;
import org.twins.core.enums.consts.SystemIds;

import java.util.UUID;

/**
 * Type-safe enumeration of system permissions. UUIDs are sourced from
 * {@link SystemIds.Permission} (single source of truth) — add new permissions there first,
 * then expose them as enum values here.
 */
@Getter
public enum Permissions {
    // GENERAL permissions
    DENY_ALL(SystemIds.Permission.General.DENY_ALL),
    SYSTEM_APP_INFO_VIEW(SystemIds.Permission.General.SYSTEM_APP_INFO_VIEW),
    LOG_SUBSTITUTION_VIEW(SystemIds.Permission.General.LOG_SUBSTITUTION_VIEW),
    ACT_AS_USER(SystemIds.Permission.General.ACT_AS_USER),
    SYSTEM_CACHE_EVICT(SystemIds.Permission.General.SYSTEM_CACHE_EVICT),

    // TWINFLOW permissions
    TWINFLOW_MANAGE(SystemIds.Permission.Twinflow.MANAGE),
    TWINFLOW_CREATE(SystemIds.Permission.Twinflow.CREATE),
    TWINFLOW_VIEW(SystemIds.Permission.Twinflow.VIEW),
    TWINFLOW_UPDATE(SystemIds.Permission.Twinflow.UPDATE),
    TWINFLOW_DELETE(SystemIds.Permission.Twinflow.DELETE),

    // TWINFLOW SCHEMA permissions
    TWINFLOW_SCHEMA_MANAGE(SystemIds.Permission.TwinflowSchema.MANAGE),
    TWINFLOW_SCHEMA_CREATE(SystemIds.Permission.TwinflowSchema.CREATE),
    TWINFLOW_SCHEMA_VIEW(SystemIds.Permission.TwinflowSchema.VIEW),
    TWINFLOW_SCHEMA_UPDATE(SystemIds.Permission.TwinflowSchema.UPDATE),
    TWINFLOW_SCHEMA_DELETE(SystemIds.Permission.TwinflowSchema.DELETE),

    // TWIN CLASS permissions
    TWIN_CLASS_MANAGE(SystemIds.Permission.TwinClass.MANAGE),
    TWIN_CLASS_CREATE(SystemIds.Permission.TwinClass.CREATE),
    TWIN_CLASS_VIEW(SystemIds.Permission.TwinClass.VIEW),
    TWIN_CLASS_UPDATE(SystemIds.Permission.TwinClass.UPDATE),
    TWIN_CLASS_DELETE(SystemIds.Permission.TwinClass.DELETE),

    // TWIN CLASS FIELD permissions
    TWIN_CLASS_FIELD_MANAGE(SystemIds.Permission.TwinClassField.MANAGE),
    TWIN_CLASS_FIELD_CREATE(SystemIds.Permission.TwinClassField.CREATE),
    TWIN_CLASS_FIELD_VIEW(SystemIds.Permission.TwinClassField.VIEW),
    TWIN_CLASS_FIELD_UPDATE(SystemIds.Permission.TwinClassField.UPDATE),
    TWIN_CLASS_FIELD_DELETE(SystemIds.Permission.TwinClassField.DELETE),

    // TWIN CLASS CARD permissions
    TWIN_CLASS_CARD_MANAGE(SystemIds.Permission.TwinClassCard.MANAGE),
    TWIN_CLASS_CARD_CREATE(SystemIds.Permission.TwinClassCard.CREATE),
    TWIN_CLASS_CARD_VIEW(SystemIds.Permission.TwinClassCard.VIEW),
    TWIN_CLASS_CARD_UPDATE(SystemIds.Permission.TwinClassCard.UPDATE),
    TWIN_CLASS_CARD_DELETE(SystemIds.Permission.TwinClassCard.DELETE),

    // TRANSITION permissions
    TRANSITION_MANAGE(SystemIds.Permission.Transition.MANAGE),
    TRANSITION_CREATE(SystemIds.Permission.Transition.CREATE),
    TRANSITION_VIEW(SystemIds.Permission.Transition.VIEW),
    TRANSITION_UPDATE(SystemIds.Permission.Transition.UPDATE),
    TRANSITION_DELETE(SystemIds.Permission.Transition.DELETE),
    TRANSITION_PERFORM(SystemIds.Permission.Transition.PERFORM),
    TRANSITION_DRAFT(SystemIds.Permission.Transition.DRAFT),

    // LINK permissions
    LINK_MANAGE(SystemIds.Permission.Link.MANAGE),
    LINK_CREATE(SystemIds.Permission.Link.CREATE),
    LINK_VIEW(SystemIds.Permission.Link.VIEW),
    LINK_UPDATE(SystemIds.Permission.Link.UPDATE),
    LINK_DELETE(SystemIds.Permission.Link.DELETE),

    // TWIN LINK permissions
    TWIN_LINK_MANAGE(SystemIds.Permission.TwinLink.MANAGE),
    TWIN_LINK_CREATE(SystemIds.Permission.TwinLink.CREATE),
    TWIN_LINK_VIEW(SystemIds.Permission.TwinLink.VIEW),
    TWIN_LINK_UPDATE(SystemIds.Permission.TwinLink.UPDATE),
    TWIN_LINK_DELETE(SystemIds.Permission.TwinLink.DELETE),

    // DOMAIN permissions
    DOMAIN_MANAGE(SystemIds.Permission.Domain.MANAGE),
    DOMAIN_CREATE(SystemIds.Permission.Domain.CREATE),
    DOMAIN_VIEW(SystemIds.Permission.Domain.VIEW),
    DOMAIN_UPDATE(SystemIds.Permission.Domain.UPDATE),
    DOMAIN_DELETE(SystemIds.Permission.Domain.DELETE),
    DOMAIN_TWINS_VIEW_ALL(SystemIds.Permission.Domain.TWINS_VIEW_ALL),
    DOMAIN_TWINS_CREATE_ANY(SystemIds.Permission.Domain.TWINS_CREATE_ANY),

    // TWIN STATUS permissions
    TWIN_STATUS_MANAGE(SystemIds.Permission.TwinStatus.MANAGE),
    TWIN_STATUS_CREATE(SystemIds.Permission.TwinStatus.CREATE),
    TWIN_STATUS_VIEW(SystemIds.Permission.TwinStatus.VIEW),
    TWIN_STATUS_UPDATE(SystemIds.Permission.TwinStatus.UPDATE),
    TWIN_STATUS_DELETE(SystemIds.Permission.TwinStatus.DELETE),

    // TWIN permissions
    TWIN_MANAGE(SystemIds.Permission.Twin.MANAGE),
    TWIN_CREATE(SystemIds.Permission.Twin.CREATE),
    TWIN_VIEW(SystemIds.Permission.Twin.VIEW),
    TWIN_UPDATE(SystemIds.Permission.Twin.UPDATE),
    TWIN_DELETE(SystemIds.Permission.Twin.DELETE),
    TWIN_SKETCH_CREATE(SystemIds.Permission.Twin.SKETCH_CREATE),

    // COMMENT permissions
    COMMENT_MANAGE(SystemIds.Permission.Comment.MANAGE),
    COMMENT_CREATE(SystemIds.Permission.Comment.CREATE),
    COMMENT_VIEW(SystemIds.Permission.Comment.VIEW),
    COMMENT_UPDATE(SystemIds.Permission.Comment.UPDATE),
    COMMENT_DELETE(SystemIds.Permission.Comment.DELETE),

    // ATTACHMENT permissions
    ATTACHMENT_MANAGE(SystemIds.Permission.Attachment.MANAGE),
    ATTACHMENT_CREATE(SystemIds.Permission.Attachment.CREATE),
    ATTACHMENT_VIEW(SystemIds.Permission.Attachment.VIEW),
    ATTACHMENT_UPDATE(SystemIds.Permission.Attachment.UPDATE),
    ATTACHMENT_DELETE(SystemIds.Permission.Attachment.DELETE),
    ATTACHMENT_VALIDATE(SystemIds.Permission.Attachment.VALIDATE),

    // USER permissions
    USER_MANAGE(SystemIds.Permission.User.MANAGE),
    USER_CREATE(SystemIds.Permission.User.CREATE),
    USER_VIEW(SystemIds.Permission.User.VIEW),
    USER_UPDATE(SystemIds.Permission.User.UPDATE),
    USER_DELETE(SystemIds.Permission.User.DELETE),

    // USER GROUP permissions
    USER_GROUP_MANAGE(SystemIds.Permission.UserGroup.MANAGE),
    USER_GROUP_CREATE(SystemIds.Permission.UserGroup.CREATE),
    USER_GROUP_VIEW(SystemIds.Permission.UserGroup.VIEW),
    USER_GROUP_UPDATE(SystemIds.Permission.UserGroup.UPDATE),
    USER_GROUP_DELETE(SystemIds.Permission.UserGroup.DELETE),

    // DATA LIST permissions
    DATA_LIST_MANAGE(SystemIds.Permission.DataList.MANAGE),
    DATA_LIST_CREATE(SystemIds.Permission.DataList.CREATE),
    DATA_LIST_VIEW(SystemIds.Permission.DataList.VIEW),
    DATA_LIST_UPDATE(SystemIds.Permission.DataList.UPDATE),
    DATA_LIST_DELETE(SystemIds.Permission.DataList.DELETE),

    // DATA LIST OPTION permissions
    DATA_LIST_OPTION_MANAGE(SystemIds.Permission.DataListOption.MANAGE),
    DATA_LIST_OPTION_CREATE(SystemIds.Permission.DataListOption.CREATE),
    DATA_LIST_OPTION_VIEW(SystemIds.Permission.DataListOption.VIEW),
    DATA_LIST_OPTION_UPDATE(SystemIds.Permission.DataListOption.UPDATE),
    DATA_LIST_OPTION_DELETE(SystemIds.Permission.DataListOption.DELETE),

    // DATA LIST SUBSET permissions
    DATA_LIST_SUBSET_MANAGE(SystemIds.Permission.DataListSubset.MANAGE),
    DATA_LIST_SUBSET_CREATE(SystemIds.Permission.DataListSubset.CREATE),
    DATA_LIST_SUBSET_VIEW(SystemIds.Permission.DataListSubset.VIEW),
    DATA_LIST_SUBSET_UPDATE(SystemIds.Permission.DataListSubset.UPDATE),
    DATA_LIST_SUBSET_DELETE(SystemIds.Permission.DataListSubset.DELETE),

    // PERMISSION permissions
    PERMISSION_MANAGE(SystemIds.Permission.PermissionEntity.MANAGE),
    PERMISSION_CREATE(SystemIds.Permission.PermissionEntity.CREATE),
    PERMISSION_VIEW(SystemIds.Permission.PermissionEntity.VIEW),
    PERMISSION_UPDATE(SystemIds.Permission.PermissionEntity.UPDATE),
    PERMISSION_DELETE(SystemIds.Permission.PermissionEntity.DELETE),

    // USER GROUP BY ASSIGNEE PROPAGATION permissions
    USER_GROUP_INVOLVE_ASSIGNEE_MANAGE(SystemIds.Permission.UserGroupInvolveAssignee.MANAGE),
    USER_GROUP_INVOLVE_ASSIGNEE_CREATE(SystemIds.Permission.UserGroupInvolveAssignee.CREATE),
    USER_GROUP_INVOLVE_ASSIGNEE_VIEW(SystemIds.Permission.UserGroupInvolveAssignee.VIEW),
    USER_GROUP_INVOLVE_ASSIGNEE_UPDATE(SystemIds.Permission.UserGroupInvolveAssignee.UPDATE),
    USER_GROUP_INVOLVE_ASSIGNEE_DELETE(SystemIds.Permission.UserGroupInvolveAssignee.DELETE),

    // PERMISSION GRANT SPACE ROLE permissions
    PERMISSION_GRANT_SPACE_ROLE_MANAGE(SystemIds.Permission.PermissionGrantSpaceRole.MANAGE),
    PERMISSION_GRANT_SPACE_ROLE_CREATE(SystemIds.Permission.PermissionGrantSpaceRole.CREATE),
    PERMISSION_GRANT_SPACE_ROLE_VIEW(SystemIds.Permission.PermissionGrantSpaceRole.VIEW),
    PERMISSION_GRANT_SPACE_ROLE_UPDATE(SystemIds.Permission.PermissionGrantSpaceRole.UPDATE),
    PERMISSION_GRANT_SPACE_ROLE_DELETE(SystemIds.Permission.PermissionGrantSpaceRole.DELETE),

    // PERMISSION GRANT TWIN ROLE permissions
    PERMISSION_GRANT_TWIN_ROLE_MANAGE(SystemIds.Permission.PermissionGrantTwinRole.MANAGE),
    PERMISSION_GRANT_TWIN_ROLE_CREATE(SystemIds.Permission.PermissionGrantTwinRole.CREATE),
    PERMISSION_GRANT_TWIN_ROLE_VIEW(SystemIds.Permission.PermissionGrantTwinRole.VIEW),
    PERMISSION_GRANT_TWIN_ROLE_UPDATE(SystemIds.Permission.PermissionGrantTwinRole.UPDATE),
    PERMISSION_GRANT_TWIN_ROLE_DELETE(SystemIds.Permission.PermissionGrantTwinRole.DELETE),

    // PERMISSION GRANT USER permissions
    PERMISSION_GRANT_USER_MANAGE(SystemIds.Permission.PermissionGrantUser.MANAGE),
    PERMISSION_GRANT_USER_CREATE(SystemIds.Permission.PermissionGrantUser.CREATE),
    PERMISSION_GRANT_USER_VIEW(SystemIds.Permission.PermissionGrantUser.VIEW),
    PERMISSION_GRANT_USER_UPDATE(SystemIds.Permission.PermissionGrantUser.UPDATE),
    PERMISSION_GRANT_USER_DELETE(SystemIds.Permission.PermissionGrantUser.DELETE),

    // PERMISSION GRANT USER GROUP permissions
    PERMISSION_GRANT_USER_GROUP_MANAGE(SystemIds.Permission.PermissionGrantUserGroup.MANAGE),
    PERMISSION_GRANT_USER_GROUP_CREATE(SystemIds.Permission.PermissionGrantUserGroup.CREATE),
    PERMISSION_GRANT_USER_GROUP_VIEW(SystemIds.Permission.PermissionGrantUserGroup.VIEW),
    PERMISSION_GRANT_USER_GROUP_UPDATE(SystemIds.Permission.PermissionGrantUserGroup.UPDATE),
    PERMISSION_GRANT_USER_GROUP_DELETE(SystemIds.Permission.PermissionGrantUserGroup.DELETE),

    // PERMISSION GROUP permissions
    PERMISSION_GROUP_MANAGE(SystemIds.Permission.PermissionGroup.MANAGE),
    PERMISSION_GROUP_CREATE(SystemIds.Permission.PermissionGroup.CREATE),
    PERMISSION_GROUP_VIEW(SystemIds.Permission.PermissionGroup.VIEW),
    PERMISSION_GROUP_UPDATE(SystemIds.Permission.PermissionGroup.UPDATE),
    PERMISSION_GROUP_DELETE(SystemIds.Permission.PermissionGroup.DELETE),

    // PERMISSION SCHEMA permissions
    PERMISSION_SCHEMA_MANAGE(SystemIds.Permission.PermissionSchema.MANAGE),
    PERMISSION_SCHEMA_CREATE(SystemIds.Permission.PermissionSchema.CREATE),
    PERMISSION_SCHEMA_VIEW(SystemIds.Permission.PermissionSchema.VIEW),
    PERMISSION_SCHEMA_UPDATE(SystemIds.Permission.PermissionSchema.UPDATE),
    PERMISSION_SCHEMA_DELETE(SystemIds.Permission.PermissionSchema.DELETE),

    // USER PERMISSION permissions
    USER_PERMISSION_MANAGE(SystemIds.Permission.UserPermission.MANAGE),
    USER_PERMISSION_CREATE(SystemIds.Permission.UserPermission.CREATE),
    USER_PERMISSION_VIEW(SystemIds.Permission.UserPermission.VIEW),
    USER_PERMISSION_UPDATE(SystemIds.Permission.UserPermission.UPDATE),
    USER_PERMISSION_DELETE(SystemIds.Permission.UserPermission.DELETE),

    // I18N permissions
    I18N_MANAGE(SystemIds.Permission.I18n.MANAGE),
    I18N_CREATE(SystemIds.Permission.I18n.CREATE),
    I18N_VIEW(SystemIds.Permission.I18n.VIEW),
    I18N_UPDATE(SystemIds.Permission.I18n.UPDATE),
    I18N_DELETE(SystemIds.Permission.I18n.DELETE),

    // FACTORY ERASER permissions
    FACTORY_ERASER_MANAGE(SystemIds.Permission.FactoryEraser.MANAGE),
    FACTORY_ERASER_CREATE(SystemIds.Permission.FactoryEraser.CREATE),
    FACTORY_ERASER_VIEW(SystemIds.Permission.FactoryEraser.VIEW),
    FACTORY_ERASER_UPDATE(SystemIds.Permission.FactoryEraser.UPDATE),
    FACTORY_ERASER_DELETE(SystemIds.Permission.FactoryEraser.DELETE),

    // FACTORY permissions
    FACTORY_MANAGE(SystemIds.Permission.Factory.MANAGE),
    FACTORY_CREATE(SystemIds.Permission.Factory.CREATE),
    FACTORY_VIEW(SystemIds.Permission.Factory.VIEW),
    FACTORY_UPDATE(SystemIds.Permission.Factory.UPDATE),
    FACTORY_DELETE(SystemIds.Permission.Factory.DELETE),

    // FACTORY MULTIPLIER permissions
    FACTORY_MULTIPLIER_MANAGE(SystemIds.Permission.FactoryMultiplier.MANAGE),
    FACTORY_MULTIPLIER_CREATE(SystemIds.Permission.FactoryMultiplier.CREATE),
    FACTORY_MULTIPLIER_VIEW(SystemIds.Permission.FactoryMultiplier.VIEW),
    FACTORY_MULTIPLIER_UPDATE(SystemIds.Permission.FactoryMultiplier.UPDATE),
    FACTORY_MULTIPLIER_DELETE(SystemIds.Permission.FactoryMultiplier.DELETE),
    FACTORY_MULTIPLIER_PARAM_MANAGE(SystemIds.Permission.FactoryMultiplier.PARAM_MANAGE),

    // FACTORY PIPELINE permissions
    FACTORY_PIPELINE_MANAGE(SystemIds.Permission.FactoryPipeline.MANAGE),
    FACTORY_PIPELINE_CREATE(SystemIds.Permission.FactoryPipeline.CREATE),
    FACTORY_PIPELINE_VIEW(SystemIds.Permission.FactoryPipeline.VIEW),
    FACTORY_PIPELINE_UPDATE(SystemIds.Permission.FactoryPipeline.UPDATE),
    FACTORY_PIPELINE_DELETE(SystemIds.Permission.FactoryPipeline.DELETE),

    // CONDITION SET permissions
    FACTORY_CONDITION_SET_MANAGE(SystemIds.Permission.FactoryConditionSet.MANAGE),
    FACTORY_CONDITION_SET_CREATE(SystemIds.Permission.FactoryConditionSet.CREATE),
    FACTORY_CONDITION_SET_VIEW(SystemIds.Permission.FactoryConditionSet.VIEW),
    FACTORY_CONDITION_SET_UPDATE(SystemIds.Permission.FactoryConditionSet.UPDATE),
    FACTORY_CONDITION_SET_DELETE(SystemIds.Permission.FactoryConditionSet.DELETE),

    // BRANCH permissions
    FACTORY_BRANCH_MANAGE(SystemIds.Permission.FactoryBranch.MANAGE),
    FACTORY_BRANCH_CREATE(SystemIds.Permission.FactoryBranch.CREATE),
    FACTORY_BRANCH_VIEW(SystemIds.Permission.FactoryBranch.VIEW),
    FACTORY_BRANCH_UPDATE(SystemIds.Permission.FactoryBranch.UPDATE),
    FACTORY_BRANCH_DELETE(SystemIds.Permission.FactoryBranch.DELETE),

    // DRAFT permissions
    DRAFT_MANAGE(SystemIds.Permission.Draft.MANAGE),
    DRAFT_CREATE(SystemIds.Permission.Draft.CREATE),
    DRAFT_VIEW(SystemIds.Permission.Draft.VIEW),
    DRAFT_UPDATE(SystemIds.Permission.Draft.UPDATE),
    DRAFT_DELETE(SystemIds.Permission.Draft.DELETE),
    DRAFT_COMMIT(SystemIds.Permission.Draft.COMMIT),

    // DOMAIN BUSINESS ACCOUNT permissions
    DOMAIN_BUSINESS_ACCOUNT_MANAGE(SystemIds.Permission.DomainBusinessAccount.MANAGE),
    DOMAIN_BUSINESS_ACCOUNT_CREATE(SystemIds.Permission.DomainBusinessAccount.CREATE),
    DOMAIN_BUSINESS_ACCOUNT_VIEW(SystemIds.Permission.DomainBusinessAccount.VIEW),
    DOMAIN_BUSINESS_ACCOUNT_UPDATE(SystemIds.Permission.DomainBusinessAccount.UPDATE),
    DOMAIN_BUSINESS_ACCOUNT_DELETE(SystemIds.Permission.DomainBusinessAccount.DELETE),

    // DOMAIN USER permissions
    DOMAIN_USER_MANAGE(SystemIds.Permission.DomainUser.MANAGE),
    DOMAIN_USER_CREATE(SystemIds.Permission.DomainUser.CREATE),
    DOMAIN_USER_VIEW(SystemIds.Permission.DomainUser.VIEW),
    DOMAIN_USER_UPDATE(SystemIds.Permission.DomainUser.UPDATE),
    DOMAIN_USER_DELETE(SystemIds.Permission.DomainUser.DELETE),

    // BUSINESS ACCOUNT permissions
    BUSINESS_ACCOUNT_MANAGE(SystemIds.Permission.BusinessAccount.MANAGE),
    BUSINESS_ACCOUNT_CREATE(SystemIds.Permission.BusinessAccount.CREATE),
    BUSINESS_ACCOUNT_VIEW(SystemIds.Permission.BusinessAccount.VIEW),
    BUSINESS_ACCOUNT_UPDATE(SystemIds.Permission.BusinessAccount.UPDATE),
    BUSINESS_ACCOUNT_DELETE(SystemIds.Permission.BusinessAccount.DELETE),

    // SPACE ROLE permissions
    SPACE_ROLE_MANAGE(SystemIds.Permission.SpaceRole.MANAGE),
    SPACE_ROLE_CREATE(SystemIds.Permission.SpaceRole.CREATE),
    SPACE_ROLE_VIEW(SystemIds.Permission.SpaceRole.VIEW),
    SPACE_ROLE_UPDATE(SystemIds.Permission.SpaceRole.UPDATE),
    SPACE_ROLE_DELETE(SystemIds.Permission.SpaceRole.DELETE),

    // FEATURER permissions
    FEATURER_MANAGE(SystemIds.Permission.Featurer.MANAGE),
    FEATURER_CREATE(SystemIds.Permission.Featurer.CREATE),
    FEATURER_VIEW(SystemIds.Permission.Featurer.VIEW),
    FEATURER_UPDATE(SystemIds.Permission.Featurer.UPDATE),
    FEATURER_DELETE(SystemIds.Permission.Featurer.DELETE),

    // TIER permissions
    TIER_MANAGE(SystemIds.Permission.Tier.MANAGE),
    TIER_CREATE(SystemIds.Permission.Tier.CREATE),
    TIER_VIEW(SystemIds.Permission.Tier.VIEW),
    TIER_UPDATE(SystemIds.Permission.Tier.UPDATE),
    TIER_DELETE(SystemIds.Permission.Tier.DELETE),

    // FACE permissions
    FACE_MANAGE(SystemIds.Permission.Face.MANAGE),
    FACE_CREATE(SystemIds.Permission.Face.CREATE),
    FACE_VIEW(SystemIds.Permission.Face.VIEW),
    FACE_UPDATE(SystemIds.Permission.Face.UPDATE),
    FACE_DELETE(SystemIds.Permission.Face.DELETE),

    // PIPELINE STEP permissions
    FACTORY_PIPELINE_STEP_MANAGE(SystemIds.Permission.FactoryPipelineStep.MANAGE),
    FACTORY_PIPELINE_STEP_CREATE(SystemIds.Permission.FactoryPipelineStep.CREATE),
    FACTORY_PIPELINE_STEP_VIEW(SystemIds.Permission.FactoryPipelineStep.VIEW),
    FACTORY_PIPELINE_STEP_UPDATE(SystemIds.Permission.FactoryPipelineStep.UPDATE),
    FACTORY_PIPELINE_STEP_DELETE(SystemIds.Permission.FactoryPipelineStep.DELETE),

    // HISTORY permissions
    HISTORY_MANAGE(SystemIds.Permission.History.MANAGE),
    HISTORY_CREATE(SystemIds.Permission.History.CREATE),
    HISTORY_VIEW(SystemIds.Permission.History.VIEW),
    HISTORY_UPDATE(SystemIds.Permission.History.UPDATE),
    HISTORY_DELETE(SystemIds.Permission.History.DELETE),
    HISTORY_MACHINE_USER_VIEW(SystemIds.Permission.History.MACHINE_USER_VIEW),

    PROJECTION_MANAGE(SystemIds.Permission.Projection.MANAGE),
    PROJECTION_CREATE(SystemIds.Permission.Projection.CREATE),
    PROJECTION_VIEW(SystemIds.Permission.Projection.VIEW),
    PROJECTION_UPDATE(SystemIds.Permission.Projection.UPDATE),
    PROJECTION_DELETE(SystemIds.Permission.Projection.DELETE),

    PROJECTION_EXCLUSION_MANAGE(SystemIds.Permission.ProjectionExclusion.MANAGE),
    PROJECTION_EXCLUSION_CREATE(SystemIds.Permission.ProjectionExclusion.CREATE),
    PROJECTION_EXCLUSION_VIEW(SystemIds.Permission.ProjectionExclusion.VIEW),
    PROJECTION_EXCLUSION_UPDATE(SystemIds.Permission.ProjectionExclusion.UPDATE),
    PROJECTION_EXCLUSION_DELETE(SystemIds.Permission.ProjectionExclusion.DELETE),

    // TWIN CLASS FIELD RULE permissions
    TWIN_CLASS_FIELD_RULE_MANAGE(SystemIds.Permission.TwinClassFieldRule.MANAGE),
    TWIN_CLASS_FIELD_RULE_CREATE(SystemIds.Permission.TwinClassFieldRule.CREATE),
    TWIN_CLASS_FIELD_RULE_VIEW(SystemIds.Permission.TwinClassFieldRule.VIEW),
    TWIN_CLASS_FIELD_RULE_UPDATE(SystemIds.Permission.TwinClassFieldRule.UPDATE),
    TWIN_CLASS_FIELD_RULE_DELETE(SystemIds.Permission.TwinClassFieldRule.DELETE),

    // TWINFLOW FACTORY permissions
    TWINFLOW_FACTORY_MANAGE(SystemIds.Permission.TwinflowFactory.MANAGE),
    TWINFLOW_FACTORY_CREATE(SystemIds.Permission.TwinflowFactory.CREATE),
    TWINFLOW_FACTORY_VIEW(SystemIds.Permission.TwinflowFactory.VIEW),
    TWINFLOW_FACTORY_UPDATE(SystemIds.Permission.TwinflowFactory.UPDATE),
    TWINFLOW_FACTORY_DELETE(SystemIds.Permission.TwinflowFactory.DELETE),

    TWIN_CLASS_FREEZE_MANAGE(SystemIds.Permission.TwinClassFreeze.MANAGE),
    TWIN_CLASS_FREEZE_CREATE(SystemIds.Permission.TwinClassFreeze.CREATE),
    TWIN_CLASS_FREEZE_VIEW(SystemIds.Permission.TwinClassFreeze.VIEW),
    TWIN_CLASS_FREEZE_UPDATE(SystemIds.Permission.TwinClassFreeze.UPDATE),
    TWIN_CLASS_FREEZE_DELETE(SystemIds.Permission.TwinClassFreeze.DELETE),

    // HISTORY NOTIFICATION permissions
    HISTORY_NOTIFICATION_MANAGE(SystemIds.Permission.HistoryNotification.MANAGE),
    HISTORY_NOTIFICATION_CREATE(SystemIds.Permission.HistoryNotification.CREATE),
    HISTORY_NOTIFICATION_VIEW(SystemIds.Permission.HistoryNotification.VIEW),
    HISTORY_NOTIFICATION_UPDATE(SystemIds.Permission.HistoryNotification.UPDATE),
    HISTORY_NOTIFICATION_DELETE(SystemIds.Permission.HistoryNotification.DELETE),

    SCHEDULER_MANAGE(SystemIds.Permission.Scheduler.MANAGE),
    SCHEDULER_CREATE(SystemIds.Permission.Scheduler.CREATE),
    SCHEDULER_VIEW(SystemIds.Permission.Scheduler.VIEW),
    SCHEDULER_UPDATE(SystemIds.Permission.Scheduler.UPDATE),
    SCHEDULER_DELETE(SystemIds.Permission.Scheduler.DELETE),

    // TWIN CLASS DYNAMIC MARKER permissions
    TWIN_CLASS_DYNAMIC_MARKER_MANAGE(SystemIds.Permission.TwinClassDynamicMarker.MANAGE),
    TWIN_CLASS_DYNAMIC_MARKER_CREATE(SystemIds.Permission.TwinClassDynamicMarker.CREATE),
    TWIN_CLASS_DYNAMIC_MARKER_VIEW(SystemIds.Permission.TwinClassDynamicMarker.VIEW),
    TWIN_CLASS_DYNAMIC_MARKER_UPDATE(SystemIds.Permission.TwinClassDynamicMarker.UPDATE),
    TWIN_CLASS_DYNAMIC_MARKER_DELETE(SystemIds.Permission.TwinClassDynamicMarker.DELETE),

    // TWIN VALIDATOR SET permissions
    TWIN_VALIDATOR_SET_MANAGE(SystemIds.Permission.TwinValidatorSet.MANAGE),
    TWIN_VALIDATOR_SET_CREATE(SystemIds.Permission.TwinValidatorSet.CREATE),
    TWIN_VALIDATOR_SET_VIEW(SystemIds.Permission.TwinValidatorSet.VIEW),
    TWIN_VALIDATOR_SET_UPDATE(SystemIds.Permission.TwinValidatorSet.UPDATE),
    TWIN_VALIDATOR_SET_DELETE(SystemIds.Permission.TwinValidatorSet.DELETE),

    // TWIN TRIGGER permissions
    TWIN_TRIGGER_MANAGE(SystemIds.Permission.TwinTrigger.MANAGE),
    TWIN_TRIGGER_CREATE(SystemIds.Permission.TwinTrigger.CREATE),
    TWIN_TRIGGER_VIEW(SystemIds.Permission.TwinTrigger.VIEW),
    TWIN_TRIGGER_UPDATE(SystemIds.Permission.TwinTrigger.UPDATE),
    TWIN_TRIGGER_DELETE(SystemIds.Permission.TwinTrigger.DELETE),

    // USER GROUP BY ACT AS USER permissions
    USER_GROUP_INVOLVE_ACT_AS_USER_MANAGE(SystemIds.Permission.UserGroupInvolveActAsUser.MANAGE),
    USER_GROUP_INVOLVE_ACT_AS_USER_CREATE(SystemIds.Permission.UserGroupInvolveActAsUser.CREATE),
    USER_GROUP_INVOLVE_ACT_AS_USER_VIEW(SystemIds.Permission.UserGroupInvolveActAsUser.VIEW),
    USER_GROUP_INVOLVE_ACT_AS_USER_UPDATE(SystemIds.Permission.UserGroupInvolveActAsUser.UPDATE),
    USER_GROUP_INVOLVE_ACT_AS_USER_DELETE(SystemIds.Permission.UserGroupInvolveActAsUser.DELETE),

    // ACTION RESTRICTION REASON permissions
    ACTION_RESTRICTION_REASON_MANAGE(SystemIds.Permission.ActionRestrictionReason.MANAGE),
    ACTION_RESTRICTION_REASON_CREATE(SystemIds.Permission.ActionRestrictionReason.CREATE),
    ACTION_RESTRICTION_REASON_VIEW(SystemIds.Permission.ActionRestrictionReason.VIEW),
    ACTION_RESTRICTION_REASON_UPDATE(SystemIds.Permission.ActionRestrictionReason.UPDATE),
    ACTION_RESTRICTION_REASON_DELETE(SystemIds.Permission.ActionRestrictionReason.DELETE),

    // NOTIFICATION SCHEMA permissions
    NOTIFICATION_SCHEMA_MANAGE(SystemIds.Permission.NotificationSchema.MANAGE),
    NOTIFICATION_SCHEMA_CREATE(SystemIds.Permission.NotificationSchema.CREATE),
    NOTIFICATION_SCHEMA_VIEW(SystemIds.Permission.NotificationSchema.VIEW),
    NOTIFICATION_SCHEMA_UPDATE(SystemIds.Permission.NotificationSchema.UPDATE),
    NOTIFICATION_SCHEMA_DELETE(SystemIds.Permission.NotificationSchema.DELETE),

    // TWIN POINTER permissions
    TWIN_POINTER_MANAGE(SystemIds.Permission.TwinPointer.MANAGE),
    TWIN_POINTER_CREATE(SystemIds.Permission.TwinPointer.CREATE),
    TWIN_POINTER_VIEW(SystemIds.Permission.TwinPointer.VIEW),
    TWIN_POINTER_UPDATE(SystemIds.Permission.TwinPointer.UPDATE),
    TWIN_POINTER_DELETE(SystemIds.Permission.TwinPointer.DELETE)
    ;

    private final UUID id;
    private final UUID permissionGroupId;

    Permissions(UUID id) {
        this.id = id;
        this.permissionGroupId = SystemIds.Permission.PERMISSION_GROUP_DEFAULT;
    }
}
