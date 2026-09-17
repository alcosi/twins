package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dto.rest.action.ActionRestrictionReasonDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionDTOv1;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.comment.CommentDTOv1;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.datalist.DataListSubsetDTOv1;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.core.dto.rest.factory.*;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerTypeDTOv1;
import org.twins.core.dto.rest.history.HistoryTypeDTOv1;
import org.twins.core.dto.rest.i18n.I18nDTOv1;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.notification.*;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupDTOv1;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.scheduler.SchedulerDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.tier.TierDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.twinpointer.TwinPointerDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSetDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.FeaturerParams;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {

    private final RestDTOMapperRegistry restDTOMapperRegistry;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv1> statusMap = new HashMap<>();
        Map<UUID, LinkDTOv1> linkMap = new HashMap<>();
        Map<UUID, TwinTriggerDTOv1> triggerMap = new HashMap<>();
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
        Map<UUID, TwinflowFactoryDTOv1> twinflowFactoryMap = new HashMap<>();
        Map<UUID, FactoryDTOv1> factoryMap = new HashMap<>();
        Map<UUID, FactoryPipelineDTOv1> factoryPipelineMap = new HashMap<>();
        Map<UUID, FactoryConditionSetDTOv1> factoryConditionSetMap = new HashMap<>();
        Map<UUID, FactoryMultiplierDTOv1> factoryMultiplierMap = new HashMap<>();
        Map<UUID, FactoryBranchDTOv1> factoryBranchMap = new HashMap<>();
        Map<UUID, FactoryPipelineStepDTOv1> factoryPipelineStepMap = new HashMap<>();
        Map<UUID, FactoryMultiplierFilterDTOv1> factoryMultiplierFilterMap = new HashMap<>();
        Map<UUID, FactoryEraserDTOv1> factoryEraserMap = new HashMap<>();
        Map<UUID, FactoryTriggerDTOv1> factoryTriggerMap = new HashMap<>();
        Map<UUID, FactoryConditionDTOv1> factoryConditionMap = new HashMap<>();
        Map<UUID, FaceDTOv1> faceMap = new HashMap<>();
        Map<UUID, CommentDTOv1> commentMap = new HashMap<>();
        Map<UUID, I18nDTOv1> i18nMap = new HashMap<>();
        Map<UUID, TwinClassSchemaDTOv1> twinClassSchemaMap = new HashMap<>();
        Map<UUID, TwinflowSchemaDTOv1> twinflowSchemaMap = new HashMap<>();
        Map<Integer, FeaturerDTOv1> featurerMap = new HashMap<>();
        Map<Integer, FeaturerTypeDTOv1> featurerTypeMap = new HashMap<>();
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
        Map<String, HistoryTypeDTOv1> historyTypeMap = new HashMap<>();
        Map<UUID, TwinValidatorSetDTOv1> twinValidatorSetMap = new HashMap<>();
        Map<UUID, ActionRestrictionReasonDTOv1> actionRestrictionReasonMap = new HashMap<>();
        Map<UUID, TwinPointerDTOv1> twinPointerMap = new HashMap<>();
        Map<UUID, DataListSubsetDTOv1> dataListSubsetMap = new HashMap<>();

        //resolve featurer param entity refs postponed during the main conversion: loaded entities are postponed
        //into their typed related maps and rendered on level 1
        restDTOMapperRegistry.getEntityRefRestDTOMapper().resolve(mapperContext);
        MapperContext mapperContextLevel2 = mapperContext.cloneIgnoreRelatedObjects();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassMap(), restDTOMapperRegistry.getTwinClassRestDTOMapper(), mapperContextLevel2, twinClassMap, TwinClassEntity::getId);
        if (!mapperContext.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinMap(), restDTOMapperRegistry.getTwinRestDTOMapperV2(), mapperContextLevel2, twinMap, TwinEntity::getId);
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinStatusMap(), restDTOMapperRegistry.getTwinStatusRestDTOMapper(), mapperContextLevel2, statusMap, TwinStatusEntity::getId);
        if (!mapperContext.getRelatedLinkMap().isEmpty())
            convertAndPut(mapperContext.getRelatedLinkMap(), restDTOMapperRegistry.getLinkRestDTOMapper(), mapperContextLevel2, linkMap, LinkEntity::getId);
        if (!mapperContext.getRelatedTwinTriggerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinTriggerMap(), restDTOMapperRegistry.getTwinTriggerRestDTOMapper(), mapperContextLevel2, triggerMap, TwinTriggerEntity::getId);
        if (!mapperContext.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserMap(), restDTOMapperRegistry.getUserRestDTOMapper(), mapperContextLevel2, userMap, UserEntity::getId);
        if (!mapperContext.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserGroupMap(), restDTOMapperRegistry.getUserGroupRestDTOMapper(), mapperContextLevel2, userGroupMap, UserGroupEntity::getId);
        if (!mapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowTransitionMap(), restDTOMapperRegistry.getTransitionBaseV1RestDTOMapper(), mapperContextLevel2, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContext.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListMap(), restDTOMapperRegistry.getDataListRestDTOMapper(), mapperContextLevel2, dataListMap, DataListEntity::getId);
        if (!mapperContext.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListOptionMap(), restDTOMapperRegistry.getDataListOptionRestDTOMapper(), mapperContextLevel2, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContext.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSpaceRoleMap(), restDTOMapperRegistry.getSpaceRoleDTOMapper(), mapperContextLevel2, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContext.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContext.getRelatedBusinessAccountMap(), restDTOMapperRegistry.getBusinessAccountDTOMapper(), mapperContextLevel2, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContext.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionGroupMap(), restDTOMapperRegistry.getPermissionGroupRestDTOMapper(), mapperContextLevel2, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContext.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionMap(), restDTOMapperRegistry.getPermissionRestDTOMapper(), mapperContextLevel2, permissionMap, PermissionEntity::getId);
        if (!mapperContext.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionSchemaMap(), restDTOMapperRegistry.getPermissionSchemaRestDTOMapper(), mapperContextLevel2, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContext.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowMap(), restDTOMapperRegistry.getTwinflowBaseV1RestDTOMapper(), mapperContextLevel2, twinflowMap, TwinflowEntity::getId);
        if (!mapperContext.getRelatedTwinflowFactoryMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowFactoryMap(), restDTOMapperRegistry.getTwinflowFactoryRestDTOMapperV1(), mapperContextLevel2, twinflowFactoryMap, TwinflowFactoryEntity::getId);
        if (!mapperContext.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryMap(), restDTOMapperRegistry.getFactoryRestDTOMapper(), mapperContextLevel2, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContext.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryPipelineMap(), restDTOMapperRegistry.getFactoryPipelineRestDTOMapper(), mapperContextLevel2, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContext.getRelatedFactoryConditionSetMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryConditionSetMap(), restDTOMapperRegistry.getFactoryConditionSetRestDTOMapper(), mapperContextLevel2, factoryConditionSetMap, TwinFactoryConditionSetEntity::getId);
        if (!mapperContext.getRelatedFactoryMultiplierMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryMultiplierMap(), restDTOMapperRegistry.getFactoryMultiplierRestDTOMapper(), mapperContextLevel2, factoryMultiplierMap, TwinFactoryMultiplierEntity::getId);
        if (!mapperContext.getRelatedFactoryBranchMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryBranchMap(), restDTOMapperRegistry.getFactoryBranchRestDTOMapper(), mapperContextLevel2, factoryBranchMap, TwinFactoryBranchEntity::getId);
        if (!mapperContext.getRelatedFactoryPipelineStepMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryPipelineStepMap(), restDTOMapperRegistry.getFactoryPipelineStepRestDTOMapper(), mapperContextLevel2, factoryPipelineStepMap, TwinFactoryPipelineStepEntity::getId);
        if (!mapperContext.getRelatedFactoryMultiplierFilterMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryMultiplierFilterMap(), restDTOMapperRegistry.getFactoryMultiplierFilterRestDTOMapper(), mapperContextLevel2, factoryMultiplierFilterMap, TwinFactoryMultiplierFilterEntity::getId);
        if (!mapperContext.getRelatedFactoryEraserMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryEraserMap(), restDTOMapperRegistry.getFactoryEraserRestDTOMapper(), mapperContextLevel2, factoryEraserMap, TwinFactoryEraserEntity::getId);
        if (!mapperContext.getRelatedFactoryTriggerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryTriggerMap(), restDTOMapperRegistry.getFactoryTriggerRestDTOMapper(), mapperContextLevel2, factoryTriggerMap, TwinFactoryTriggerEntity::getId);
        if (!mapperContext.getRelatedFactoryConditionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryConditionMap(), restDTOMapperRegistry.getFactoryConditionRestDTOMapper(), mapperContextLevel2, factoryConditionMap, TwinFactoryConditionEntity::getId);
        if (!mapperContext.getRelatedFaceMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFaceMap(), restDTOMapperRegistry.getFaceRestDTOMapper(), mapperContextLevel2, faceMap, FaceEntity::getId);
        if (!mapperContext.getRelatedCommentMap().isEmpty())
            convertAndPut(mapperContext.getRelatedCommentMap(), restDTOMapperRegistry.getCommentRestDTOMapper(), mapperContextLevel2, commentMap, TwinCommentEntity::getId);
        if (!mapperContext.getRelatedI18nMap().isEmpty())
            convertAndPut(mapperContext.getRelatedI18nMap(), restDTOMapperRegistry.getI18nRestDTOMapper(), mapperContextLevel2, i18nMap, I18nEntity::getId);
        if (!mapperContext.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFeaturerMap(), restDTOMapperRegistry.getFeaturerRestDTOMapper(), mapperContextLevel2, featurerMap, FeaturerEntity::getId);
        if (!mapperContext.getRelatedFeaturerTypeMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFeaturerTypeMap(), restDTOMapperRegistry.getFeaturerTypeRestDTOMapper(), mapperContextLevel2, featurerTypeMap, FeaturerTypeEntity::getId);
        if (!mapperContext.getRelatedTwinClassFieldMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassFieldMap(), restDTOMapperRegistry.getTwinClassFieldRestDTOMapper(), mapperContextLevel2, twinClassFiledMap, TwinClassFieldEntity::getId);
        if (!mapperContext.getRelatedTwinClassSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassSchemaMap(), restDTOMapperRegistry.getTwinClassSchemaDTOMapper(), mapperContextLevel2, twinClassSchemaMap, TwinClassSchemaEntity::getId);
        if (!mapperContext.getRelatedTwinflowSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowSchemaMap(), restDTOMapperRegistry.getTwinflowSchemaRestDTOMapper(), mapperContextLevel2, twinflowSchemaMap, TwinflowSchemaEntity::getId);
        if (!mapperContext.getRelatedTierMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTierMap(), restDTOMapperRegistry.getTierRestDTOMapper(), mapperContextLevel2, tierMap, TierEntity::getId);
        if (!mapperContext.getRelatedAttachmentRestrictionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedAttachmentRestrictionMap(), restDTOMapperRegistry.getAttachmentRestrictionRestDTOMapper(), mapperContextLevel2, attachmentRestrictionMap, TwinAttachmentRestrictionEntity::getId);
        if (!mapperContext.getRelatedTwinClassFreezeMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassFreezeMap(), restDTOMapperRegistry.getTwinClassFreezeDTOMapper(), mapperContextLevel2, twinClassFreezeMap, TwinClassFreezeEntity::getId);
        if (!mapperContext.getRelatedClassFieldRuleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedClassFieldRuleMap(), restDTOMapperRegistry.getTwinClassFieldRuleRestDTOMapper(), mapperContextLevel2, twinClassFieldRuleMap, TwinClassFieldRuleEntity::getId);
        if (!mapperContext.getRelatedProjectionTypeGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedProjectionTypeGroupMap(), restDTOMapperRegistry.getProjectionTypeGroupRestDTOMapper(), mapperContextLevel2, projectionTypeGroupMap, ProjectionTypeGroupEntity::getId);
        if (!mapperContext.getRelatedProjectionTypeMap().isEmpty())
            convertAndPut(mapperContext.getRelatedProjectionTypeMap(), restDTOMapperRegistry.getProjectionTypeRestDTOMapper(), mapperContextLevel2, projectionTypeMap, ProjectionTypeEntity::getId);
        if (!mapperContext.getRelatedSchedulerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSchedulerMap(), restDTOMapperRegistry.getSchedulerRestDTOMapperV1(), mapperContextLevel2, schedulerMap, SchedulerEntity::getId);
        if (!mapperContext.getRelatedHistoryNotificationRecipientMap().isEmpty())
            convertAndPut(mapperContext.getRelatedHistoryNotificationRecipientMap(), restDTOMapperRegistry.getHistoryNotificationRecipientDTOMapper(), mapperContextLevel2, historyNotificationRecipientMap, HistoryNotificationRecipientEntity::getId);
        if (!mapperContext.getRelatedNotificationSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationSchemaMap(), restDTOMapperRegistry.getNotificationSchemaRestDTOMapper(), mapperContextLevel2, notificationSchemaMap, NotificationSchemaEntity::getId);
        if (!mapperContext.getRelatedNotificationChannelMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationChannelMap(), restDTOMapperRegistry.getNotificationChannelRestDTOMapper(), mapperContextLevel2, notificationChannelMap, NotificationChannelEntity::getId);
        if (!mapperContext.getRelatedNotificationContextMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationContextMap(), restDTOMapperRegistry.getNotificationContextRestDTOMapper(), mapperContextLevel2, notificationContextMap, NotificationContextEntity::getId);
        if (!mapperContext.getRelatedNotificationChannelEventMap().isEmpty())
            convertAndPut(mapperContext.getRelatedNotificationChannelEventMap(), restDTOMapperRegistry.getNotificationChannelEventRestDTOMapper(), mapperContextLevel2, notificationChannelEventMap, NotificationChannelEventEntity::getId);
        if (!mapperContext.getRelatedTwinValidatorSetMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinValidatorSetMap(), restDTOMapperRegistry.getTwinValidatorSetRestDTOMapper(), mapperContextLevel2, twinValidatorSetMap, TwinValidatorSetEntity::getId);
        if (!mapperContext.getRelatedHistoryTypeMap().isEmpty())
            convertAndPut(mapperContext.getRelatedHistoryTypeMap(), restDTOMapperRegistry.getHistoryTypeRestDTOMapper(), mapperContextLevel2, historyTypeMap, HistoryTypeEntity::getId);
        if (!mapperContext.getRelatedActionRestrictionReasonMap().isEmpty())
            convertAndPut(mapperContext.getRelatedActionRestrictionReasonMap(), restDTOMapperRegistry.getActionRestrictionReasonRestDTOMapper(), mapperContextLevel2, actionRestrictionReasonMap, ActionRestrictionReasonEntity::getId);
        if (!mapperContext.getRelatedTwinPointerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinPointerMap(), restDTOMapperRegistry.getTwinPointerRestDTOMapper(), mapperContextLevel2, twinPointerMap, TwinPointerEntity::getId);
        if (!mapperContext.getRelatedDataListSubsetMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListSubsetMap(), restDTOMapperRegistry.getDataListSubsetRestDTOMapper(), mapperContextLevel2, dataListSubsetMap, DataListSubsetEntity::getId);
        if (!mapperContext.getRelatedFeaturerParamsMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFeaturerParamsMap(), restDTOMapperRegistry.getFeaturerParametrizedRestDTOMapper(), mapperContextLevel2, featurerMap, FeaturerParams::getFeaturerId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        MapperContext mapperContextLevel3 = mapperContextLevel2.cloneIgnoreRelatedObjects();
        //resolve entity refs postponed during level 1 conversions (e.g. featurer params pairs): rendered on level 2
        restDTOMapperRegistry.getEntityRefRestDTOMapper().resolve(mapperContextLevel2);
        if (!mapperContextLevel2.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassMap(), restDTOMapperRegistry.getTwinClassRestDTOMapper(), mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinMap(), restDTOMapperRegistry.getTwinRestDTOMapperV2(), mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinStatusMap(), restDTOMapperRegistry.getTwinStatusRestDTOMapper(), mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel2.getRelatedLinkMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedLinkMap(), restDTOMapperRegistry.getLinkRestDTOMapper(), mapperContextLevel3, linkMap, LinkEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinTriggerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinTriggerMap(), restDTOMapperRegistry.getTwinTriggerRestDTOMapper(), mapperContextLevel3, triggerMap, TwinTriggerEntity::getId);
        if (!mapperContextLevel2.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedUserMap(), restDTOMapperRegistry.getUserRestDTOMapper(), mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel2.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedUserGroupMap(), restDTOMapperRegistry.getUserGroupRestDTOMapper(), mapperContextLevel3, userGroupMap, UserGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowTransitionMap(), restDTOMapperRegistry.getTransitionBaseV1RestDTOMapper(), mapperContextLevel3, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListMap(), restDTOMapperRegistry.getDataListRestDTOMapper(), mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListOptionMap(), restDTOMapperRegistry.getDataListOptionRestDTOMapper(), mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel2.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedSpaceRoleMap(), restDTOMapperRegistry.getSpaceRoleDTOMapper(), mapperContextLevel3, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContextLevel2.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedBusinessAccountMap(), restDTOMapperRegistry.getBusinessAccountDTOMapper(), mapperContextLevel3, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionGroupMap(), restDTOMapperRegistry.getPermissionGroupRestDTOMapper(), mapperContextLevel3, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionMap(), restDTOMapperRegistry.getPermissionRestDTOMapper(), mapperContextLevel3, permissionMap, PermissionEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionSchemaMap(), restDTOMapperRegistry.getPermissionSchemaRestDTOMapper(), mapperContextLevel3, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowMap(), restDTOMapperRegistry.getTwinflowBaseV1RestDTOMapper(), mapperContextLevel3, twinflowMap, TwinflowEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowFactoryMap(), restDTOMapperRegistry.getTwinflowFactoryRestDTOMapperV1(), mapperContextLevel3, twinflowFactoryMap, TwinflowFactoryEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryMap(), restDTOMapperRegistry.getFactoryRestDTOMapper(), mapperContextLevel3, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryPipelineMap(), restDTOMapperRegistry.getFactoryPipelineRestDTOMapper(), mapperContextLevel3, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryConditionSetMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryConditionSetMap(), restDTOMapperRegistry.getFactoryConditionSetRestDTOMapper(), mapperContextLevel3, factoryConditionSetMap, TwinFactoryConditionSetEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryMultiplierMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryMultiplierMap(), restDTOMapperRegistry.getFactoryMultiplierRestDTOMapper(), mapperContextLevel3, factoryMultiplierMap, TwinFactoryMultiplierEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryBranchMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryBranchMap(), restDTOMapperRegistry.getFactoryBranchRestDTOMapper(), mapperContextLevel3, factoryBranchMap, TwinFactoryBranchEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryPipelineStepMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryPipelineStepMap(), restDTOMapperRegistry.getFactoryPipelineStepRestDTOMapper(), mapperContextLevel3, factoryPipelineStepMap, TwinFactoryPipelineStepEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryMultiplierFilterMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryMultiplierFilterMap(), restDTOMapperRegistry.getFactoryMultiplierFilterRestDTOMapper(), mapperContextLevel3, factoryMultiplierFilterMap, TwinFactoryMultiplierFilterEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryEraserMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryEraserMap(), restDTOMapperRegistry.getFactoryEraserRestDTOMapper(), mapperContextLevel3, factoryEraserMap, TwinFactoryEraserEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryTriggerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryTriggerMap(), restDTOMapperRegistry.getFactoryTriggerRestDTOMapper(), mapperContextLevel3, factoryTriggerMap, TwinFactoryTriggerEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryConditionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryConditionMap(), restDTOMapperRegistry.getFactoryConditionRestDTOMapper(), mapperContextLevel3, factoryConditionMap, TwinFactoryConditionEntity::getId);
        if (!mapperContextLevel2.getRelatedFaceMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFaceMap(), restDTOMapperRegistry.getFaceRestDTOMapper(), mapperContextLevel3, faceMap, FaceEntity::getId);
        if (!mapperContextLevel2.getRelatedCommentMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedCommentMap(), restDTOMapperRegistry.getCommentRestDTOMapper(), mapperContextLevel3, commentMap, TwinCommentEntity::getId);
        if (!mapperContextLevel2.getRelatedI18nMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedI18nMap(), restDTOMapperRegistry.getI18nRestDTOMapper(), mapperContextLevel3, i18nMap, I18nEntity::getId);
        if (!mapperContextLevel2.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFeaturerMap(), restDTOMapperRegistry.getFeaturerRestDTOMapper(), mapperContextLevel3, featurerMap, FeaturerEntity::getId);
        if (!mapperContextLevel2.getRelatedFeaturerTypeMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFeaturerTypeMap(), restDTOMapperRegistry.getFeaturerTypeRestDTOMapper(), mapperContextLevel3, featurerTypeMap, FeaturerTypeEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinClassFieldMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassFieldMap(), restDTOMapperRegistry.getTwinClassFieldRestDTOMapper(), mapperContextLevel3, twinClassFiledMap, TwinClassFieldEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinClassSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassSchemaMap(), restDTOMapperRegistry.getTwinClassSchemaDTOMapper(), mapperContextLevel3, twinClassSchemaMap, TwinClassSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowSchemaMap(), restDTOMapperRegistry.getTwinflowSchemaRestDTOMapper(), mapperContextLevel3, twinflowSchemaMap, TwinflowSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedTierMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTierMap(), restDTOMapperRegistry.getTierRestDTOMapper(), mapperContextLevel3, tierMap, TierEntity::getId);
        if (!mapperContextLevel2.getRelatedAttachmentRestrictionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedAttachmentRestrictionMap(), restDTOMapperRegistry.getAttachmentRestrictionRestDTOMapper(), mapperContextLevel3, attachmentRestrictionMap, TwinAttachmentRestrictionEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinClassFreezeMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassFreezeMap(), restDTOMapperRegistry.getTwinClassFreezeDTOMapper(), mapperContextLevel3, twinClassFreezeMap, TwinClassFreezeEntity::getId);
        if (!mapperContextLevel2.getRelatedClassFieldRuleMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedClassFieldRuleMap(), restDTOMapperRegistry.getTwinClassFieldRuleRestDTOMapper(), mapperContextLevel3, twinClassFieldRuleMap, TwinClassFieldRuleEntity::getId);
        if (!mapperContextLevel2.getRelatedProjectionTypeGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedProjectionTypeGroupMap(), restDTOMapperRegistry.getProjectionTypeGroupRestDTOMapper(), mapperContextLevel3, projectionTypeGroupMap, ProjectionTypeGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedProjectionTypeMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedProjectionTypeMap(), restDTOMapperRegistry.getProjectionTypeRestDTOMapper(), mapperContextLevel3, projectionTypeMap, ProjectionTypeEntity::getId);
        if (!mapperContextLevel2.getRelatedSchedulerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedSchedulerMap(), restDTOMapperRegistry.getSchedulerRestDTOMapperV1(), mapperContextLevel3, schedulerMap, SchedulerEntity::getId);
        if (!mapperContextLevel2.getRelatedHistoryNotificationRecipientMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedHistoryNotificationRecipientMap(), restDTOMapperRegistry.getHistoryNotificationRecipientDTOMapper(), mapperContextLevel3, historyNotificationRecipientMap, HistoryNotificationRecipientEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationSchemaMap(), restDTOMapperRegistry.getNotificationSchemaRestDTOMapper(), mapperContextLevel3, notificationSchemaMap, NotificationSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationChannelMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationChannelMap(), restDTOMapperRegistry.getNotificationChannelRestDTOMapper(), mapperContextLevel3, notificationChannelMap, NotificationChannelEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationContextMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationContextMap(), restDTOMapperRegistry.getNotificationContextRestDTOMapper(), mapperContextLevel3, notificationContextMap, NotificationContextEntity::getId);
        if (!mapperContextLevel2.getRelatedNotificationChannelEventMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedNotificationChannelEventMap(), restDTOMapperRegistry.getNotificationChannelEventRestDTOMapper(), mapperContextLevel3, notificationChannelEventMap, NotificationChannelEventEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinValidatorSetMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinValidatorSetMap(), restDTOMapperRegistry.getTwinValidatorSetRestDTOMapper(), mapperContextLevel3, twinValidatorSetMap, TwinValidatorSetEntity::getId);
        if (!mapperContextLevel2.getRelatedHistoryTypeMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedHistoryTypeMap(), restDTOMapperRegistry.getHistoryTypeRestDTOMapper(), mapperContextLevel3, historyTypeMap, HistoryTypeEntity::getId);
        if (!mapperContextLevel2.getRelatedActionRestrictionReasonMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedActionRestrictionReasonMap(), restDTOMapperRegistry.getActionRestrictionReasonRestDTOMapper(), mapperContextLevel3, actionRestrictionReasonMap, ActionRestrictionReasonEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinPointerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinPointerMap(), restDTOMapperRegistry.getTwinPointerRestDTOMapper(), mapperContextLevel3, twinPointerMap, TwinPointerEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListSubsetMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListSubsetMap(), restDTOMapperRegistry.getDataListSubsetRestDTOMapper(), mapperContextLevel3, dataListSubsetMap, DataListSubsetEntity::getId);
        if (!mapperContextLevel2.getRelatedFeaturerParamsMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFeaturerParamsMap(), restDTOMapperRegistry.getFeaturerParametrizedRestDTOMapper(), mapperContextLevel3, featurerMap, FeaturerParams::getFeaturerId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        //this level was added because of dataLists. In case of search twins, twinClass will be detected on level1, twinClass.tagDataList will be detected on level2 and list options for tagDataList will be detected only on level3
        mapperContextLevel3.setLazyRelations(true); // on such depth we will not collect related objects anymore
        //resolve entity refs postponed during level 2 conversions: loaded and rendered on this level, the cascade
        //stops here because postpone is a no-op with lazyRelations=true
        restDTOMapperRegistry.getEntityRefRestDTOMapper().resolve(mapperContextLevel3);
        if (!mapperContextLevel3.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassMap(), restDTOMapperRegistry.getTwinClassRestDTOMapper(), mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinMap(), restDTOMapperRegistry.getTwinRestDTOMapperV2(), mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinStatusMap(), restDTOMapperRegistry.getTwinStatusRestDTOMapper(), mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel3.getRelatedLinkMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedLinkMap(), restDTOMapperRegistry.getLinkRestDTOMapper(), mapperContextLevel3, linkMap, LinkEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinTriggerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinTriggerMap(), restDTOMapperRegistry.getTwinTriggerRestDTOMapper(), mapperContextLevel3, triggerMap, TwinTriggerEntity::getId);
        if (!mapperContextLevel3.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedUserMap(), restDTOMapperRegistry.getUserRestDTOMapper(), mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel3.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedUserGroupMap(), restDTOMapperRegistry.getUserGroupRestDTOMapper(), mapperContextLevel3, userGroupMap, UserGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowTransitionMap(), restDTOMapperRegistry.getTransitionBaseV1RestDTOMapper(), mapperContextLevel3, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListMap(), restDTOMapperRegistry.getDataListRestDTOMapper(), mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListOptionMap(), restDTOMapperRegistry.getDataListOptionRestDTOMapper(), mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel3.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedSpaceRoleMap(), restDTOMapperRegistry.getSpaceRoleDTOMapper(), mapperContextLevel3, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContextLevel3.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedBusinessAccountMap(), restDTOMapperRegistry.getBusinessAccountDTOMapper(), mapperContextLevel3, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionGroupMap(), restDTOMapperRegistry.getPermissionGroupRestDTOMapper(), mapperContextLevel3, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionMap(), restDTOMapperRegistry.getPermissionRestDTOMapper(), mapperContextLevel3, permissionMap, PermissionEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionSchemaMap(), restDTOMapperRegistry.getPermissionSchemaRestDTOMapper(), mapperContextLevel3, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowMap(), restDTOMapperRegistry.getTwinflowBaseV1RestDTOMapper(), mapperContextLevel3, twinflowMap, TwinflowEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowFactoryMap(), restDTOMapperRegistry.getTwinflowFactoryRestDTOMapperV1(), mapperContextLevel3, twinflowFactoryMap, TwinflowFactoryEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryMap(), restDTOMapperRegistry.getFactoryRestDTOMapper(), mapperContextLevel3, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryPipelineMap(), restDTOMapperRegistry.getFactoryPipelineRestDTOMapper(), mapperContextLevel3, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryConditionSetMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryConditionSetMap(), restDTOMapperRegistry.getFactoryConditionSetRestDTOMapper(), mapperContextLevel3, factoryConditionSetMap, TwinFactoryConditionSetEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryMultiplierMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryMultiplierMap(), restDTOMapperRegistry.getFactoryMultiplierRestDTOMapper(), mapperContextLevel3, factoryMultiplierMap, TwinFactoryMultiplierEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryBranchMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryBranchMap(), restDTOMapperRegistry.getFactoryBranchRestDTOMapper(), mapperContextLevel3, factoryBranchMap, TwinFactoryBranchEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryPipelineStepMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryPipelineStepMap(), restDTOMapperRegistry.getFactoryPipelineStepRestDTOMapper(), mapperContextLevel3, factoryPipelineStepMap, TwinFactoryPipelineStepEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryMultiplierFilterMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryMultiplierFilterMap(), restDTOMapperRegistry.getFactoryMultiplierFilterRestDTOMapper(), mapperContextLevel3, factoryMultiplierFilterMap, TwinFactoryMultiplierFilterEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryEraserMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryEraserMap(), restDTOMapperRegistry.getFactoryEraserRestDTOMapper(), mapperContextLevel3, factoryEraserMap, TwinFactoryEraserEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryTriggerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryTriggerMap(), restDTOMapperRegistry.getFactoryTriggerRestDTOMapper(), mapperContextLevel3, factoryTriggerMap, TwinFactoryTriggerEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryConditionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryConditionMap(), restDTOMapperRegistry.getFactoryConditionRestDTOMapper(), mapperContextLevel3, factoryConditionMap, TwinFactoryConditionEntity::getId);
        if (!mapperContextLevel3.getRelatedFaceMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFaceMap(), restDTOMapperRegistry.getFaceRestDTOMapper(), mapperContextLevel3, faceMap, FaceEntity::getId);
        if (!mapperContextLevel3.getRelatedCommentMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedCommentMap(), restDTOMapperRegistry.getCommentRestDTOMapper(), mapperContextLevel3, commentMap, TwinCommentEntity::getId);
        if (!mapperContext.getRelatedI18nMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedI18nMap(), restDTOMapperRegistry.getI18nRestDTOMapper(), mapperContextLevel3, i18nMap, I18nEntity::getId);
        if (!mapperContextLevel3.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFeaturerMap(), restDTOMapperRegistry.getFeaturerRestDTOMapper(), mapperContextLevel3, featurerMap, FeaturerEntity::getId);
        if (!mapperContextLevel3.getRelatedFeaturerTypeMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFeaturerTypeMap(), restDTOMapperRegistry.getFeaturerTypeRestDTOMapper(), mapperContextLevel3, featurerTypeMap, FeaturerTypeEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinClassFieldMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassFieldMap(), restDTOMapperRegistry.getTwinClassFieldRestDTOMapper(), mapperContextLevel3, twinClassFiledMap, TwinClassFieldEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinClassSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassSchemaMap(), restDTOMapperRegistry.getTwinClassSchemaDTOMapper(), mapperContextLevel3, twinClassSchemaMap, TwinClassSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowSchemaMap(), restDTOMapperRegistry.getTwinflowSchemaRestDTOMapper(), mapperContextLevel3, twinflowSchemaMap, TwinflowSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedTierMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTierMap(), restDTOMapperRegistry.getTierRestDTOMapper(), mapperContextLevel3, tierMap, TierEntity::getId);
        if (!mapperContextLevel3.getRelatedAttachmentRestrictionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedAttachmentRestrictionMap(), restDTOMapperRegistry.getAttachmentRestrictionRestDTOMapper(), mapperContextLevel3, attachmentRestrictionMap, TwinAttachmentRestrictionEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinClassFreezeMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassFreezeMap(), restDTOMapperRegistry.getTwinClassFreezeDTOMapper(), mapperContextLevel3, twinClassFreezeMap, TwinClassFreezeEntity::getId);
        if (!mapperContextLevel3.getRelatedClassFieldRuleMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedClassFieldRuleMap(), restDTOMapperRegistry.getTwinClassFieldRuleRestDTOMapper(), mapperContextLevel3, twinClassFieldRuleMap, TwinClassFieldRuleEntity::getId);
        if (!mapperContextLevel3.getRelatedProjectionTypeGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedProjectionTypeGroupMap(), restDTOMapperRegistry.getProjectionTypeGroupRestDTOMapper(), mapperContextLevel3, projectionTypeGroupMap, ProjectionTypeGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedProjectionTypeMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedProjectionTypeMap(), restDTOMapperRegistry.getProjectionTypeRestDTOMapper(), mapperContextLevel3, projectionTypeMap, ProjectionTypeEntity::getId);
        if (!mapperContextLevel3.getRelatedSchedulerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedSchedulerMap(), restDTOMapperRegistry.getSchedulerRestDTOMapperV1(), mapperContextLevel3, schedulerMap, SchedulerEntity::getId);
        if (!mapperContextLevel3.getRelatedHistoryNotificationRecipientMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedHistoryNotificationRecipientMap(), restDTOMapperRegistry.getHistoryNotificationRecipientDTOMapper(), mapperContextLevel3, historyNotificationRecipientMap, HistoryNotificationRecipientEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationSchemaMap(), restDTOMapperRegistry.getNotificationSchemaRestDTOMapper(), mapperContextLevel3, notificationSchemaMap, NotificationSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationChannelMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationChannelMap(), restDTOMapperRegistry.getNotificationChannelRestDTOMapper(), mapperContextLevel3, notificationChannelMap, NotificationChannelEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationContextMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationContextMap(), restDTOMapperRegistry.getNotificationContextRestDTOMapper(), mapperContextLevel3, notificationContextMap, NotificationContextEntity::getId);
        if (!mapperContextLevel3.getRelatedNotificationChannelEventMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedNotificationChannelEventMap(), restDTOMapperRegistry.getNotificationChannelEventRestDTOMapper(), mapperContextLevel3, notificationChannelEventMap, NotificationChannelEventEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinValidatorSetMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinValidatorSetMap(), restDTOMapperRegistry.getTwinValidatorSetRestDTOMapper(), mapperContextLevel3, twinValidatorSetMap, TwinValidatorSetEntity::getId);
        if (!mapperContextLevel3.getRelatedHistoryTypeMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedHistoryTypeMap(), restDTOMapperRegistry.getHistoryTypeRestDTOMapper(), mapperContextLevel3, historyTypeMap, HistoryTypeEntity::getId);
        if (!mapperContextLevel3.getRelatedActionRestrictionReasonMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedActionRestrictionReasonMap(), restDTOMapperRegistry.getActionRestrictionReasonRestDTOMapper(), mapperContextLevel3, actionRestrictionReasonMap, ActionRestrictionReasonEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinPointerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinPointerMap(), restDTOMapperRegistry.getTwinPointerRestDTOMapper(), mapperContextLevel3, twinPointerMap, TwinPointerEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListSubsetMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListSubsetMap(), restDTOMapperRegistry.getDataListSubsetRestDTOMapper(), mapperContextLevel3, dataListSubsetMap, DataListSubsetEntity::getId);
        if (!mapperContextLevel3.getRelatedFeaturerParamsMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFeaturerParamsMap(), restDTOMapperRegistry.getFeaturerParametrizedRestDTOMapper(), mapperContextLevel3, featurerMap, FeaturerParams::getFeaturerId);

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setLinkMap(linkMap.isEmpty() ? null : linkMap)
                .setTriggerMap(triggerMap.isEmpty() ? null : triggerMap)
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
                .setTwinflowFactoryMap(twinflowFactoryMap.isEmpty() ? null : twinflowFactoryMap)
                .setFactoryMap(factoryMap.isEmpty() ? null : factoryMap)
                .setFactoryPipelineMap(factoryPipelineMap.isEmpty() ? null : factoryPipelineMap)
                .setFactoryConditionSetMap(factoryConditionSetMap.isEmpty() ? null : factoryConditionSetMap)
                .setFactoryMultiplierMap(factoryMultiplierMap.isEmpty() ? null : factoryMultiplierMap)
                .setFactoryBranchMap(factoryBranchMap.isEmpty() ? null : factoryBranchMap)
                .setFactoryPipelineStepMap(factoryPipelineStepMap.isEmpty() ? null : factoryPipelineStepMap)
                .setFactoryMultiplierFilterMap(factoryMultiplierFilterMap.isEmpty() ? null : factoryMultiplierFilterMap)
                .setFactoryEraserMap(factoryEraserMap.isEmpty() ? null : factoryEraserMap)
                .setFactoryTriggerMap(factoryTriggerMap.isEmpty() ? null : factoryTriggerMap)
                .setFactoryConditionMap(factoryConditionMap.isEmpty() ? null : factoryConditionMap)
                .setCommentMap(commentMap.isEmpty() ? null : commentMap)
                .setFeaturerMap(featurerMap.isEmpty() ? null : featurerMap)
                .setFeaturerTypeMap(featurerTypeMap.isEmpty() ? null : featurerTypeMap)
                .setFaceMap(faceMap.isEmpty() ? null : faceMap)
                .setI18nMap(i18nMap.isEmpty() ? null : i18nMap)
                .setTwinClassFieldMap(twinClassFiledMap.isEmpty() ? null : twinClassFiledMap)
                .setTwinClassSchemaMap(twinClassSchemaMap.isEmpty() ? null : twinClassSchemaMap)
                .setTwinflowSchemaMap(twinflowSchemaMap.isEmpty() ? null : twinflowSchemaMap)
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
                .setHistoryTypeMap(historyTypeMap.isEmpty() ? null : historyTypeMap)
                .setTwinValidatorSetMap(twinValidatorSetMap.isEmpty() ? null : twinValidatorSetMap)
                .setActionRestrictionReasonMap(actionRestrictionReasonMap.isEmpty() ? null : actionRestrictionReasonMap)
                .setTwinPointerMap(twinPointerMap.isEmpty() ? null : twinPointerMap)
                .setDataListSubsetMap(dataListSubsetMap.isEmpty() ? null : dataListSubsetMap)
        ;
        return ret;
    }

    public <E, D, K, KD> void convertAndPut(Map<K, RelatedObject<E>> relatedObjects, RestSimpleDTOMapper<E, ? extends D> mapper, MapperContext mapperContext, Map<KD, D> map, Function<? super E, ? extends KD> functionGetId) throws Exception {
        for (RelatedObject<E> relatedObject : relatedObjects.values())
            map.put(functionGetId.apply(relatedObject.getObject()), mapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
    }
}
