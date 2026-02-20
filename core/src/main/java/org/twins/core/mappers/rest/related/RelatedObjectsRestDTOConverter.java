package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dao.notification.NotificationContextEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionDTOv1;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.comment.CommentDTOv1;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv1;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.i18n.I18nDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientDTOv1;
import org.twins.core.dto.rest.notification.NotificationChannelDTOv1;
import org.twins.core.dto.rest.notification.NotificationChannelEventDTOv1;
import org.twins.core.dto.rest.notification.NotificationContextDTOv1;
import org.twins.core.dto.rest.notification.NotificationSchemaDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSetDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupDTOv1;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.scheduler.SchedulerDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.tier.TierDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientDTOMapperV1;
import org.twins.core.mappers.rest.notification.NotificationChannelEventRestDTOMapper;
import org.twins.core.mappers.rest.notification.NotificationChannelRestDTOMapper;
import org.twins.core.mappers.rest.notification.NotificationContextRestDTOMapper;
import org.twins.core.mappers.rest.notification.NotificationSchemaRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.scheduler.SchedulerRestDTOMapperV1;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.*;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {

    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;
    private final DataListRestDTOMapper dataListRestDTOMapper;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;
    private final TwinClassSchemaDTOMapper twinClassSchemaDTOMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FaceRestDTOMapper faceRestDTOMapper;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final CommentRestDTOMapper commentRestDTOMapper;
    private final I18nRestDTOMapper i18nRestDTOMapper;
    private final TierRestDTOMapper tierRestDTOMapper;
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;
    private final TwinClassFreezeDTOMapper twinClassFreezeDTOMapper;
    private final TwinClassFieldRuleRestDTOMapper twinClassFieldRuleRestDTOMapper;
    private final ProjectionTypeGroupRestDTOMapper projectionTypeGroupRestDTOMapper;
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;
    private final SchedulerRestDTOMapperV1 schedulerRestDTOMapperV1;
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapper;
    private final NotificationSchemaRestDTOMapper notificationSchemaRestDTOMapper;
    private final NotificationChannelRestDTOMapper notificationChannelRestDTOMapper;
    private final NotificationContextRestDTOMapper notificationContextRestDTOMapper;
    private final NotificationChannelEventRestDTOMapper notificationChannelEventRestDTOMapper;
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv1> statusMap = new HashMap<>();
        Map<UUID, UserDTOv1> userMap = new HashMap<>();
        Map<UUID, UserGroupDTOv1> userGroupMap = new HashMap<>();
        Map<UUID, TwinClassDTOv1> twinClassMap = new HashMap<>();
        Map<UUID, TwinflowTransitionBaseDTOv1> twinflowTransitionMap = new HashMap<>();
        Map<UUID, DataListDTOv1> dataListMap = new HashMap<>();
        Map<UUID, DataListOptionDTOv1> dataListOptionMap = new HashMap<>();
        Map<UUID, SpaceRoleDTOv1> spaceRoleMap = new HashMap<>();
        Map<UUID, BusinessAccountDTOv1> businessAccountMap = new HashMap<>();
        Map<UUID, PermissionGroupDTOv1> permissionGroupMap = new HashMap<>();
        Map<UUID, PermissionDTOv1> permissionMap = new HashMap<>();
        Map<UUID, PermissionSchemaDTOv1> permissionSchemaMap = new HashMap<>();
        Map<UUID, TwinflowBaseDTOv1> twinflowMap = new HashMap<>();
        Map<UUID, FactoryDTOv1> factoryMap = new HashMap<>();
        Map<UUID, FactoryPipelineDTOv1> factoryPipelineMap = new HashMap<>();
        Map<UUID, FactoryConditionSetDTOv1> factoryConditionSetMap = new HashMap<>();
        Map<UUID, FactoryMultiplierDTOv1> factoryMultiplierMap = new HashMap<>();
        Map<UUID, FaceDTOv1> faceMap = new HashMap<>();
        Map<UUID, CommentDTOv1> commentMap = new HashMap<>();
        Map<UUID, I18nDTOv1> i18nMap = new HashMap<>();
        Map<UUID, TwinClassSchemaDTOv1> twinClassSchemaMap = new HashMap<>();
        Map<Integer, FeaturerDTOv1> featurerMap = new HashMap<>();
        Map<UUID, TwinClassFieldDTOv1> twinClassFiledMap = new HashMap<>();
        Map<UUID, TierDTOv1> tierMap = new HashMap<>();
        Map<UUID, AttachmentRestrictionDTOv1> attachmentRestrictionMap = new HashMap<>();
        Map<UUID, TwinClassFreezeDTOv1> twinClassFreezeMap = new HashMap<>();
        Map<UUID, TwinClassFieldRuleDTOv1> twinClassFieldRuleMap = new HashMap<>();
        Map<UUID, ProjectionTypeGroupDTOv1> projectionTypeGroupMap = new HashMap<>();
        Map<UUID, ProjectionTypeDTOv1> projectionTypeMap = new HashMap<>();
        Map<UUID, SchedulerDTOv1> schedulerMap = new HashMap<>();
        Map<UUID, HistoryNotificationRecipientDTOv1> historyNotificationRecipientMap = new HashMap<>();
        Map<UUID, NotificationSchemaDTOv1> notificationSchemaMap = new HashMap<>();
        Map<UUID, NotificationChannelDTOv1> notificationChannelMap = new HashMap<>();
        Map<UUID, NotificationContextDTOv1> notificationContextMap = new HashMap<>();
        Map<UUID, NotificationChannelEventDTOv1> notificationChannelEventMap = new HashMap<>();
        Map<UUID, TwinValidatorSetDTOv1> twinValidatorSetMap = new HashMap<>();

        MapperContext mapperContextLevel2 = mapperContext.cloneIgnoreRelatedObjects();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel2, twinClassMap, TwinClassEntity::getId);
        if (!mapperContext.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel2, twinMap, TwinEntity::getId);
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, mapperContextLevel2, statusMap, TwinStatusEntity::getId);
        if (!mapperContext.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel2, userMap, UserEntity::getId);
        if (!mapperContext.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserGroupMap(), userGroupRestDTOMapper, mapperContextLevel2, userGroupMap, UserGroupEntity::getId);
        if (!mapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowTransitionMap(), transitionBaseV1RestDTOMapper, mapperContextLevel2, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContext.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel2, dataListMap, DataListEntity::getId);
        if (!mapperContext.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel2, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContext.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel2, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContext.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContext.getRelatedBusinessAccountMap(), businessAccountDTOMapper, mapperContextLevel2, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContext.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionGroupMap(), permissionGroupRestDTOMapper, mapperContextLevel2, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContext.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionMap(), permissionRestDTOMapper, mapperContextLevel2, permissionMap, PermissionEntity::getId);
        if (!mapperContext.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionSchemaMap(), permissionSchemaRestDTOMapper, mapperContextLevel2, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContext.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowMap(), twinflowBaseV1RestDTOMapper, mapperContextLevel2, twinflowMap, TwinflowEntity::getId);
        if (!mapperContext.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryMap(), factoryRestDTOMapper, mapperContextLevel2, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContext.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryPipelineMap(), factoryPipelineRestDTOMapper, mapperContextLevel2, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContext.getRelatedFactoryConditionSetMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryConditionSetMap(), factoryConditionSetRestDTOMapper, mapperContextLevel2, factoryConditionSetMap, TwinFactoryConditionSetEntity::getId);
        if (!mapperContext.getRelatedFactoryMultiplierMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryMultiplierMap(), factoryMultiplierRestDTOMapper, mapperContextLevel2, factoryMultiplierMap, TwinFactoryMultiplierEntity::getId);
        if (!mapperContext.getRelatedFaceMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFaceMap(), faceRestDTOMapper, mapperContextLevel2, faceMap, FaceEntity::getId);
        if (!mapperContext.getRelatedCommentMap().isEmpty())
            convertAndPut(mapperContext.getRelatedCommentMap(), commentRestDTOMapper, mapperContextLevel2, commentMap, TwinCommentEntity::getId);
        if (!mapperContext.getRelatedI18nMap().isEmpty())
            convertAndPut(mapperContext.getRelatedI18nMap(), i18nRestDTOMapper, mapperContextLevel2, i18nMap, I18nEntity::getId);
        if (!mapperContext.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFeaturerMap(), featurerRestDTOMapper, mapperContextLevel2, featurerMap, FeaturerEntity::getId);
        if (!mapperContext.getRelatedTwinClassFieldMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassFieldMap(), twinClassFieldRestDTOMapper, mapperContextLevel2, twinClassFiledMap, TwinClassFieldEntity::getId);
        if (!mapperContext.getRelatedTwinClassSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassSchemaMap(), twinClassSchemaDTOMapper, mapperContextLevel2, twinClassSchemaMap, TwinClassSchemaEntity::getId);
        if (!mapperContext.getRelatedTierMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTierMap(), tierRestDTOMapper, mapperContextLevel2, tierMap, TierEntity::getId);
        if (!mapperContext.getRelatedAttachmentRestrictionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedAttachmentRestrictionMap(), attachmentRestrictionRestDTOMapper, mapperContextLevel2, attachmentRestrictionMap, TwinAttachmentRestrictionEntity::getId);
        if (!mapperContext.getRelatedTwinClassFreezeMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassFreezeMap(), twinClassFreezeDTOMapper, mapperContextLevel2, twinClassFreezeMap, TwinClassFreezeEntity::getId);
        if (!mapperContext.getRelatedClassFieldRuleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedClassFieldRuleMap(), twinClassFieldRuleRestDTOMapper, mapperContextLevel2, twinClassFieldRuleMap, TwinClassFieldRuleEntity::getId);
        if (!mapperContext.getRelatedProjectionTypeGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedProjectionTypeGroupMap(), projectionTypeGroupRestDTOMapper, mapperContextLevel2, projectionTypeGroupMap, ProjectionTypeGroupEntity::getId);
        if (!mapperContext.getRelatedProjectionTypeMap().isEmpty())
            convertAndPut(mapperContext.getRelatedProjectionTypeMap(), projectionTypeRestDTOMapper, mapperContextLevel2, projectionTypeMap, ProjectionTypeEntity::getId);
        if (!mapperContext.getRelatedSchedulerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSchedulerMap(), schedulerRestDTOMapperV1, mapperContextLevel2, schedulerMap, SchedulerEntity::getId);
        if (!mapperContext.getRelatedHistoryNotificationRecipientMap().isEmpty())
            convertAndPut(mapperContext.getRelatedHistoryNotificationRecipientMap(), historyNotificationRecipientDTOMapper, mapperContextLevel2, historyNotificationRecipientMap, HistoryNotificationRecipientEntity::getId);
        if (!mapperContext.getRelatedNotificationSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationSchemaMap(), notificationSchemaRestDTOMapper, mapperContextLevel2, notificationSchemaMap, NotificationSchemaEntity::getId);
        if (!mapperContext.getRelatedNotificationChannelMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationChannelMap(), notificationChannelRestDTOMapper, mapperContextLevel2, notificationChannelMap, NotificationChannelEntity::getId);
        if (!mapperContext.getRelatedNotificationContextMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationContextMap(), notificationContextRestDTOMapper, mapperContextLevel2, notificationContextMap, NotificationContextEntity::getId);
        if (!mapperContext.getRelatedNotificationChannelEventMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationChannelEventMap(), notificationChannelEventRestDTOMapper, mapperContextLevel2, notificationChannelEventMap, NotificationChannelEventEntity::getId);
        if (!mapperContext.getRelatedTwinValidatorSetMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinValidatorSetMap(), twinValidatorSetRestDTOMapper, mapperContextLevel2, twinValidatorSetMap, TwinValidatorSetEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        MapperContext mapperContextLevel3 = mapperContextLevel2.cloneIgnoreRelatedObjects();
        if (!mapperContextLevel2.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel2.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel2.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedUserGroupMap(), userGroupRestDTOMapper, mapperContextLevel3, userGroupMap, UserGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowTransitionMap(), transitionBaseV1RestDTOMapper, mapperContextLevel3, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel2.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel3, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContextLevel2.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedBusinessAccountMap(), businessAccountDTOMapper, mapperContextLevel3, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionGroupMap(), permissionGroupRestDTOMapper, mapperContextLevel3, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionMap(), permissionRestDTOMapper, mapperContextLevel3, permissionMap, PermissionEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionSchemaMap(), permissionSchemaRestDTOMapper, mapperContextLevel3, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowMap(), twinflowBaseV1RestDTOMapper, mapperContextLevel3, twinflowMap, TwinflowEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryMap(), factoryRestDTOMapper, mapperContextLevel3, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryPipelineMap(), factoryPipelineRestDTOMapper, mapperContextLevel3, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryConditionSetMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryConditionSetMap(), factoryConditionSetRestDTOMapper, mapperContextLevel3, factoryConditionSetMap, TwinFactoryConditionSetEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryMultiplierMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryMultiplierMap(), factoryMultiplierRestDTOMapper, mapperContextLevel3, factoryMultiplierMap, TwinFactoryMultiplierEntity::getId);
        if (!mapperContextLevel2.getRelatedFaceMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFaceMap(), faceRestDTOMapper, mapperContextLevel3, faceMap, FaceEntity::getId);
        if (!mapperContextLevel2.getRelatedCommentMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedCommentMap(), commentRestDTOMapper, mapperContextLevel3, commentMap, TwinCommentEntity::getId);
        if (!mapperContextLevel2.getRelatedI18nMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedI18nMap(), i18nRestDTOMapper, mapperContextLevel3, i18nMap, I18nEntity::getId);
        if (!mapperContextLevel2.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFeaturerMap(), featurerRestDTOMapper, mapperContextLevel3, featurerMap, FeaturerEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinClassFieldMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassFieldMap(), twinClassFieldRestDTOMapper, mapperContextLevel3, twinClassFiledMap, TwinClassFieldEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinClassSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassSchemaMap(), twinClassSchemaDTOMapper, mapperContextLevel3, twinClassSchemaMap, TwinClassSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedTierMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTierMap(), tierRestDTOMapper, mapperContextLevel3, tierMap, TierEntity::getId);
        if (!mapperContextLevel2.getRelatedAttachmentRestrictionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedAttachmentRestrictionMap(), attachmentRestrictionRestDTOMapper, mapperContextLevel3, attachmentRestrictionMap, TwinAttachmentRestrictionEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinClassFreezeMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassFreezeMap(), twinClassFreezeDTOMapper, mapperContextLevel3, twinClassFreezeMap, TwinClassFreezeEntity::getId);
        if (!mapperContextLevel2.getRelatedClassFieldRuleMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedClassFieldRuleMap(), twinClassFieldRuleRestDTOMapper, mapperContextLevel3, twinClassFieldRuleMap, TwinClassFieldRuleEntity::getId);
        if (!mapperContextLevel2.getRelatedProjectionTypeGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedProjectionTypeGroupMap(), projectionTypeGroupRestDTOMapper, mapperContextLevel3, projectionTypeGroupMap, ProjectionTypeGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedProjectionTypeMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedProjectionTypeMap(), projectionTypeRestDTOMapper, mapperContextLevel3, projectionTypeMap, ProjectionTypeEntity::getId);
        if (!mapperContextLevel2.getRelatedSchedulerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedSchedulerMap(), schedulerRestDTOMapperV1, mapperContextLevel3, schedulerMap, SchedulerEntity::getId);
        if (!mapperContextLevel2.getRelatedHistoryNotificationRecipientMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedHistoryNotificationRecipientMap(), historyNotificationRecipientDTOMapper, mapperContextLevel3, historyNotificationRecipientMap, HistoryNotificationRecipientEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationSchemaMap(), notificationSchemaRestDTOMapper, mapperContextLevel3, notificationSchemaMap, NotificationSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationChannelMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationChannelMap(), notificationChannelRestDTOMapper, mapperContextLevel3, notificationChannelMap, NotificationChannelEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationContextMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationContextMap(), notificationContextRestDTOMapper, mapperContextLevel3, notificationContextMap, NotificationContextEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationChannelEventMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationChannelEventMap(), notificationChannelEventRestDTOMapper, mapperContextLevel3, notificationChannelEventMap, NotificationChannelEventEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinValidatorSetMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinValidatorSetMap(), twinValidatorSetRestDTOMapper, mapperContextLevel3, twinValidatorSetMap, TwinValidatorSetEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        //this level was added because of dataLists. In case of search twins, twinClass will be detected on level1, twinClass.tagDataList will be detected on level2 and list options for tagDataList will be detected only on level3
        mapperContextLevel3.setLazyRelations(true); // on such depth we will not collect related objects anymore
        if (!mapperContextLevel3.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel3.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel3.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedUserGroupMap(), userGroupRestDTOMapper, mapperContextLevel3, userGroupMap, UserGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowTransitionMap(), transitionBaseV1RestDTOMapper, mapperContextLevel3, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel3.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel3, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContextLevel3.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedBusinessAccountMap(), businessAccountDTOMapper, mapperContextLevel3, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionGroupMap(), permissionGroupRestDTOMapper, mapperContextLevel3, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionMap(), permissionRestDTOMapper, mapperContextLevel3, permissionMap, PermissionEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionSchemaMap(), permissionSchemaRestDTOMapper, mapperContextLevel3, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowMap(), twinflowBaseV1RestDTOMapper, mapperContextLevel3, twinflowMap, TwinflowEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryMap(), factoryRestDTOMapper, mapperContextLevel3, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryPipelineMap(), factoryPipelineRestDTOMapper, mapperContextLevel3, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryConditionSetMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryConditionSetMap(), factoryConditionSetRestDTOMapper, mapperContextLevel3, factoryConditionSetMap, TwinFactoryConditionSetEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryMultiplierMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryMultiplierMap(), factoryMultiplierRestDTOMapper, mapperContextLevel3, factoryMultiplierMap, TwinFactoryMultiplierEntity::getId);
        if (!mapperContextLevel3.getRelatedFaceMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFaceMap(), faceRestDTOMapper, mapperContextLevel3, faceMap, FaceEntity::getId);
        if (!mapperContextLevel3.getRelatedCommentMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedCommentMap(), commentRestDTOMapper, mapperContextLevel3, commentMap, TwinCommentEntity::getId);
        if (!mapperContext.getRelatedI18nMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedI18nMap(), i18nRestDTOMapper, mapperContextLevel3, i18nMap, I18nEntity::getId);
        if (!mapperContextLevel3.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFeaturerMap(), featurerRestDTOMapper, mapperContextLevel3, featurerMap, FeaturerEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinClassFieldMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassFieldMap(), twinClassFieldRestDTOMapper, mapperContextLevel3, twinClassFiledMap, TwinClassFieldEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinClassSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassSchemaMap(), twinClassSchemaDTOMapper, mapperContextLevel3, twinClassSchemaMap, TwinClassSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedTierMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTierMap(), tierRestDTOMapper, mapperContextLevel3, tierMap, TierEntity::getId);
        if (!mapperContextLevel3.getRelatedAttachmentRestrictionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedAttachmentRestrictionMap(), attachmentRestrictionRestDTOMapper, mapperContextLevel3, attachmentRestrictionMap, TwinAttachmentRestrictionEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinClassFreezeMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassFreezeMap(), twinClassFreezeDTOMapper, mapperContextLevel3, twinClassFreezeMap, TwinClassFreezeEntity::getId);
        if (!mapperContextLevel3.getRelatedClassFieldRuleMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedClassFieldRuleMap(), twinClassFieldRuleRestDTOMapper, mapperContextLevel3, twinClassFieldRuleMap, TwinClassFieldRuleEntity::getId);
        if (!mapperContextLevel3.getRelatedProjectionTypeGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedProjectionTypeGroupMap(), projectionTypeGroupRestDTOMapper, mapperContextLevel3, projectionTypeGroupMap, ProjectionTypeGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedProjectionTypeMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedProjectionTypeMap(), projectionTypeRestDTOMapper, mapperContextLevel3, projectionTypeMap, ProjectionTypeEntity::getId);
        if (!mapperContextLevel3.getRelatedSchedulerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedSchedulerMap(), schedulerRestDTOMapperV1, mapperContextLevel3, schedulerMap, SchedulerEntity::getId);
        if (!mapperContextLevel3.getRelatedHistoryNotificationRecipientMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedHistoryNotificationRecipientMap(), historyNotificationRecipientDTOMapper, mapperContextLevel3, historyNotificationRecipientMap, HistoryNotificationRecipientEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationSchemaMap(), notificationSchemaRestDTOMapper, mapperContextLevel3, notificationSchemaMap, NotificationSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationChannelMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationChannelMap(), notificationChannelRestDTOMapper, mapperContextLevel3, notificationChannelMap, NotificationChannelEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationContextMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationContextMap(), notificationContextRestDTOMapper, mapperContextLevel3, notificationContextMap, NotificationContextEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationChannelEventMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationChannelEventMap(), notificationChannelEventRestDTOMapper, mapperContextLevel3, notificationChannelEventMap, NotificationChannelEventEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinValidatorSetMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinValidatorSetMap(), twinValidatorSetRestDTOMapper, mapperContextLevel3, twinValidatorSetMap, TwinValidatorSetEntity::getId);

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setUserMap(userMap.isEmpty() ? null : userMap)
                .setUserGroupMap(userGroupMap.isEmpty() ? null : userGroupMap)
                .setTransitionsMap(twinflowTransitionMap.isEmpty() ? null : twinflowTransitionMap)
                .setDataListsMap(dataListMap.isEmpty() ? null : dataListMap)
                .setDataListsOptionMap(dataListOptionMap.isEmpty() ? null : dataListOptionMap)
                .setSpaceRoleMap(spaceRoleMap.isEmpty() ? null : spaceRoleMap)
                .setBusinessAccountMap(businessAccountMap.isEmpty() ? null : businessAccountMap)
                .setPermissionGroupMap(permissionGroupMap.isEmpty() ? null : permissionGroupMap)
                .setPermissionMap(permissionMap.isEmpty() ? null : permissionMap)
                .setPermissionSchemaMap(permissionSchemaMap.isEmpty() ? null : permissionSchemaMap)
                .setTwinflowMap(twinflowMap.isEmpty() ? null : twinflowMap)
                .setFactoryMap(factoryMap.isEmpty() ? null : factoryMap)
                .setFactoryPipelineMap(factoryPipelineMap.isEmpty() ? null : factoryPipelineMap)
                .setFactoryConditionSetMap(factoryConditionSetMap.isEmpty() ? null : factoryConditionSetMap)
                .setFactoryMultiplierMap(factoryMultiplierMap.isEmpty() ? null : factoryMultiplierMap)
                .setCommentMap(commentMap.isEmpty() ? null : commentMap)
                .setFeaturerMap(featurerMap.isEmpty() ? null : featurerMap)
                .setFaceMap(faceMap.isEmpty() ? null : faceMap)
                .setI18nMap(i18nMap.isEmpty() ? null : i18nMap)
                .setTwinClassFieldMap(twinClassFiledMap.isEmpty() ? null : twinClassFiledMap)
                .setTwinClassSchemaMap(twinClassSchemaMap.isEmpty() ? null : twinClassSchemaMap)
                .setTierMap(tierMap.isEmpty() ? null : tierMap)
                .setAttachmentRestrictionMap(attachmentRestrictionMap.isEmpty() ? null : attachmentRestrictionMap)
                .setTwinClassFreezeMap(twinClassFreezeMap.isEmpty() ? null : twinClassFreezeMap)
                .setFieldRuleMap(twinClassFieldRuleMap.isEmpty() ? null : twinClassFieldRuleMap)
                .setProjectionTypeGroupMap(projectionTypeGroupMap.isEmpty() ? null : projectionTypeGroupMap)
                .setProjectionTypeMap(projectionTypeMap.isEmpty() ? null : projectionTypeMap)
                .setSchedulerMap(schedulerMap.isEmpty() ? null : schedulerMap)
                .setHistoryNotificationRecipientMap(historyNotificationRecipientMap.isEmpty() ? null : historyNotificationRecipientMap)
                .setNotificationSchemaMap(notificationSchemaMap.isEmpty() ? null : notificationSchemaMap)
                .setNotificationChannelMap(notificationChannelMap.isEmpty() ? null : notificationChannelMap)
                .setNotificationContextMap(notificationContextMap.isEmpty() ? null : notificationContextMap)
                .setNotificationChannelEventMap(notificationChannelEventMap.isEmpty() ? null : notificationChannelEventMap)
                .setTwinValidatorSetMap(twinValidatorSetMap.isEmpty() ? null : twinValidatorSetMap)
        ;
        return ret;
    }

    public <E, D, K> void convertAndPut(Map<K, RelatedObject<E>> relatedObjects, RestSimpleDTOMapper<E, ? extends D> mapper, MapperContext mapperContext, Map<K, D> map, Function<? super E, ? extends K> functionGetId) throws Exception {
        for (RelatedObject<E> relatedObject : relatedObjects.values())
            map.put(functionGetId.apply(relatedObject.getObject()), mapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
    }
}
