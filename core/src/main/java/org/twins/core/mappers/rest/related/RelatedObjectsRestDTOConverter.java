package org.twins.core.mappers.rest.related;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
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
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.i18n.I18nEntity;
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
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonRestDTOMapper;
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
import org.twins.core.mappers.rest.history.HistoryTypeRestDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.notification.*;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.scheduler.SchedulerRestDTOMapperV1;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.*;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {

    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
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
    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;
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
    private final HistoryTypeRestDTOMapper historyTypeRestDTOMapper;
    private final ActionRestrictionReasonRestDTOMapper actionRestrictionReasonRestDTOMapper;

    private List<MappingDescriptor<?, ?, ?>> mappingDescriptors;

    private static final int CONVERSION_DEPTH = 3;

    @PostConstruct
    private void initMappingDescriptors() {
        mappingDescriptors = List.of(
                descriptor(MapperContext::getRelatedTwinClassMap,                   twinClassRestDTOMapper,                 (dto, map) -> dto.setTwinClassMap(map),                  TwinClassEntity::getId),
                descriptor(MapperContext::getRelatedTwinMap,                        twinRestDTOMapperV2,                    (dto, map) -> dto.setTwinMap(map),                       TwinEntity::getId),
                descriptor(MapperContext::getRelatedTwinStatusMap,                  twinStatusRestDTOMapper,                (dto, map) -> dto.setStatusMap(map),                     TwinStatusEntity::getId),
                descriptor(MapperContext::getRelatedTwinTriggerMap,                 twinTriggerRestDTOMapper,               (dto, map) -> dto.setTriggerMap(map),                    TwinTriggerEntity::getId),
                descriptor(MapperContext::getRelatedUserMap,                        userRestDTOMapper,                      (dto, map) -> dto.setUserMap(map),                       UserEntity::getId),
                descriptor(MapperContext::getRelatedUserGroupMap,                   userGroupRestDTOMapper,                 (dto, map) -> dto.setUserGroupMap(map),                  UserGroupEntity::getId),
                descriptor(MapperContext::getRelatedTwinflowTransitionMap,          transitionBaseV1RestDTOMapper,           (dto, map) -> dto.setTransitionsMap(map),                TwinflowTransitionEntity::getId),
                descriptor(MapperContext::getRelatedDataListMap,                    dataListRestDTOMapper,                  (dto, map) -> dto.setDataListsMap(map),                  DataListEntity::getId),
                descriptor(MapperContext::getRelatedDataListOptionMap,              dataListOptionRestDTOMapper,             (dto, map) -> dto.setDataListsOptionMap(map),            DataListOptionEntity::getId),
                descriptor(MapperContext::getRelatedSpaceRoleMap,                   spaceRoleDTOMapper,                     (dto, map) -> dto.setSpaceRoleMap(map),                  SpaceRoleEntity::getId),
                descriptor(MapperContext::getRelatedBusinessAccountMap,             businessAccountDTOMapper,               (dto, map) -> dto.setBusinessAccountMap(map),            BusinessAccountEntity::getId),
                descriptor(MapperContext::getRelatedPermissionGroupMap,             permissionGroupRestDTOMapper,           (dto, map) -> dto.setPermissionGroupMap(map),            PermissionGroupEntity::getId),
                descriptor(MapperContext::getRelatedPermissionMap,                  permissionRestDTOMapper,                (dto, map) -> dto.setPermissionMap(map),                 PermissionEntity::getId),
                descriptor(MapperContext::getRelatedPermissionSchemaMap,            permissionSchemaRestDTOMapper,           (dto, map) -> dto.setPermissionSchemaMap(map),           PermissionSchemaEntity::getId),
                descriptor(MapperContext::getRelatedTwinflowMap,                    twinflowBaseV1RestDTOMapper,             (dto, map) -> dto.setTwinflowMap(map),                   TwinflowEntity::getId),
                descriptor(MapperContext::getRelatedFactoryMap,                     factoryRestDTOMapper,                   (dto, map) -> dto.setFactoryMap(map),                    TwinFactoryEntity::getId),
                descriptor(MapperContext::getRelatedFactoryPipelineMap,             factoryPipelineRestDTOMapper,            (dto, map) -> dto.setFactoryPipelineMap(map),            TwinFactoryPipelineEntity::getId),
                descriptor(MapperContext::getRelatedFactoryConditionSetMap,         factoryConditionSetRestDTOMapper,        (dto, map) -> dto.setFactoryConditionSetMap(map),        TwinFactoryConditionSetEntity::getId),
                descriptor(MapperContext::getRelatedFactoryMultiplierMap,           factoryMultiplierRestDTOMapper,          (dto, map) -> dto.setFactoryMultiplierMap(map),          TwinFactoryMultiplierEntity::getId),
                descriptor(MapperContext::getRelatedFaceMap,                        faceRestDTOMapper,                      (dto, map) -> dto.setFaceMap(map),                       FaceEntity::getId),
                descriptor(MapperContext::getRelatedCommentMap,                     commentRestDTOMapper,                   (dto, map) -> dto.setCommentMap(map),                    TwinCommentEntity::getId),
                descriptor(MapperContext::getRelatedI18nMap,                        i18nRestDTOMapper,                      (dto, map) -> dto.setI18nMap(map),                       I18nEntity::getId),
                descriptor(MapperContext::getRelatedFeaturerMap,                    featurerRestDTOMapper,                  (dto, map) -> dto.setFeaturerMap(map),                   FeaturerEntity::getId),
                descriptor(MapperContext::getRelatedTwinClassFieldMap,              twinClassFieldRestDTOMapper,             (dto, map) -> dto.setTwinClassFieldMap(map),             TwinClassFieldEntity::getId),
                descriptor(MapperContext::getRelatedTwinClassSchemaMap,             twinClassSchemaDTOMapper,               (dto, map) -> dto.setTwinClassSchemaMap(map),            TwinClassSchemaEntity::getId),
                descriptor(MapperContext::getRelatedTwinflowSchemaMap,              twinflowSchemaRestDTOMapper,             (dto, map) -> dto.setTwinflowSchemaMap(map),             TwinflowSchemaEntity::getId),
                descriptor(MapperContext::getRelatedTierMap,                        tierRestDTOMapper,                      (dto, map) -> dto.setTierMap(map),                       TierEntity::getId),
                descriptor(MapperContext::getRelatedAttachmentRestrictionMap,       attachmentRestrictionRestDTOMapper,      (dto, map) -> dto.setAttachmentRestrictionMap(map),      TwinAttachmentRestrictionEntity::getId),
                descriptor(MapperContext::getRelatedTwinClassFreezeMap,             twinClassFreezeDTOMapper,               (dto, map) -> dto.setTwinClassFreezeMap(map),            TwinClassFreezeEntity::getId),
                descriptor(MapperContext::getRelatedClassFieldRuleMap,              twinClassFieldRuleRestDTOMapper,         (dto, map) -> dto.setFieldRuleMap(map),                  TwinClassFieldRuleEntity::getId),
                descriptor(MapperContext::getRelatedProjectionTypeGroupMap,         projectionTypeGroupRestDTOMapper,        (dto, map) -> dto.setProjectionTypeGroupMap(map),        ProjectionTypeGroupEntity::getId),
                descriptor(MapperContext::getRelatedProjectionTypeMap,              projectionTypeRestDTOMapper,             (dto, map) -> dto.setProjectionTypeMap(map),             ProjectionTypeEntity::getId),
                descriptor(MapperContext::getRelatedSchedulerMap,                   schedulerRestDTOMapperV1,               (dto, map) -> dto.setSchedulerMap(map),                  SchedulerEntity::getId),
                descriptor(MapperContext::getRelatedHistoryNotificationRecipientMap, historyNotificationRecipientDTOMapper,  (dto, map) -> dto.setHistoryNotificationRecipientMap(map), HistoryNotificationRecipientEntity::getId),
                descriptor(MapperContext::getRelatedNotificationSchemaMap,          notificationSchemaRestDTOMapper,         (dto, map) -> dto.setNotificationSchemaMap(map),         NotificationSchemaEntity::getId),
                descriptor(MapperContext::getRelatedNotificationChannelMap,         notificationChannelRestDTOMapper,        (dto, map) -> dto.setNotificationChannelMap(map),        NotificationChannelEntity::getId),
                descriptor(MapperContext::getRelatedNotificationContextMap,         notificationContextRestDTOMapper,        (dto, map) -> dto.setNotificationContextMap(map),        NotificationContextEntity::getId),
                descriptor(MapperContext::getRelatedNotificationChannelEventMap,    notificationChannelEventRestDTOMapper,   (dto, map) -> dto.setNotificationChannelEventMap(map),   NotificationChannelEventEntity::getId),
                descriptor(MapperContext::getRelatedTwinValidatorSetMap,            twinValidatorSetRestDTOMapper,           (dto, map) -> dto.setTwinValidatorSetMap(map),           TwinValidatorSetEntity::getId),
                descriptor(MapperContext::getRelatedHistoryTypeMap,                 historyTypeRestDTOMapper,               (dto, map) -> dto.setHistoryTypeMap(map),                HistoryTypeEntity::getId),
                descriptor(MapperContext::getRelatedActionRestrictionReasonMap,     actionRestrictionReasonRestDTOMapper,    (dto, map) -> dto.setActionRestrictionReasonMap(map),    ActionRestrictionReasonEntity::getId)
        );
    }

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        RelatedObjectsDTOv1 result = new RelatedObjectsDTOv1();

        MapperContext level2 = mapperContext.cloneIgnoreRelatedObjects();
        MapperContext level3 = level2.cloneIgnoreRelatedObjects();
        level3.setLazyRelations(true);

        MapperContext[] sourceContexts = {mapperContext, level2, level3};
        MapperContext[] targetContexts = {level2, level3, level3};

        Map<Integer, Map<?, ?>> resultMaps = new HashMap<>();
        for (int level = 0; level < CONVERSION_DEPTH; level++) {
            for (int i = 0; i < mappingDescriptors.size(); i++) {
                processDescriptor(mappingDescriptors.get(i), sourceContexts[level], targetContexts[level], resultMaps, i);
            }
        }

        for (int i = 0; i < mappingDescriptors.size(); i++) {
            applyResult(mappingDescriptors.get(i), result, resultMaps.get(i));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <E, D, K> void processDescriptor(
            MappingDescriptor<?, ?, ?> raw,
            MapperContext sourceCtx,
            MapperContext targetCtx,
            Map<Integer, Map<?, ?>> resultMaps,
            int index) throws Exception {
        MappingDescriptor<E, D, K> desc = (MappingDescriptor<E, D, K>) raw;
        Map<K, RelatedObject<E>> sourceMap = desc.sourceMapGetter.apply(sourceCtx);
        if (sourceMap.isEmpty())
            return;
        Map<K, D> targetMap = (Map<K, D>) resultMaps.computeIfAbsent(index, k -> new HashMap<>());
        convertAndPut(sourceMap, desc.mapper, targetCtx, targetMap, desc.idExtractor);
    }

    @SuppressWarnings("unchecked")
    private <E, D, K> void applyResult(
            MappingDescriptor<?, ?, ?> raw,
            RelatedObjectsDTOv1 result,
            Map<?, ?> map) {
        if (map == null)
            return;
        MappingDescriptor<E, D, K> desc = (MappingDescriptor<E, D, K>) raw;
        desc.resultSetter.accept(result, (Map<K, D>) map);
    }

    public <E, D, K> void convertAndPut(Map<K, RelatedObject<E>> relatedObjects, RestSimpleDTOMapper<E, ? extends D> mapper, MapperContext mapperContext, Map<K, D> map, Function<? super E, ? extends K> functionGetId) throws Exception {
        for (RelatedObject<E> relatedObject : relatedObjects.values())
            map.put(functionGetId.apply(relatedObject.getObject()), mapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
    }

    private record MappingDescriptor<E, D, K>(
            Function<MapperContext, Map<K, RelatedObject<E>>> sourceMapGetter,
            RestSimpleDTOMapper<E, ? extends D> mapper,
            BiConsumer<RelatedObjectsDTOv1, Map<K, D>> resultSetter,
            Function<? super E, ? extends K> idExtractor) {
    }

    private <E, D, K> MappingDescriptor<E, D, K> descriptor(
            Function<MapperContext, Map<K, RelatedObject<E>>> sourceMapGetter,
            RestSimpleDTOMapper<E, ? extends D> mapper,
            BiConsumer<RelatedObjectsDTOv1, Map<K, D>> resultSetter,
            Function<? super E, ? extends K> idExtractor) {
        return new MappingDescriptor<>(sourceMapGetter, mapper, resultSetter, idExtractor);
    }
}
