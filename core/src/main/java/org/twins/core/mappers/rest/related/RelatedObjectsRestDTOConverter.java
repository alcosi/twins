package org.twins.core.mappers.rest.related;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
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
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.FeaturerParams;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;
import org.twins.core.service.EntityServiceRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Converts the postponed related objects of a MapperContext into {@link RelatedObjectsDTOv1}.
 * Declarative: one {@link RelatedMapDescriptor} per related map (source getter on MapperContext, mapper from
 * EntityRestMapperRegistry, id getter, destination setter) and a single drain loop running on each of the
 * three conversion levels — related objects of level N postpone their own relations into the context of
 * level N+1 (see the inline comments about the levels).
 * EntityRefs postponed by featurer params resolution are drained between the levels by EntityRefRestDTOMapper.
 */
@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {

    private final EntityRestMapperRegistry entityRestMapperRegistry;
    private final EntityServiceRegistry entityServiceRegistry;
    private final EntityRefRestDTOMapper entityRefRestDTOMapper;

    private List<RelatedMapDescriptor<?, ?, ?, ?>> descriptors;

    @PostConstruct
    void initDescriptors() {
        //featurer definitions (postponed as FeaturerEntity or as FeaturerParams pairs) share one accumulator:
        //the owning descriptor applies the setter, the pairs descriptor drains into the same map with a null setter
        Map<Integer, FeaturerDTOv1> featurerMap = new HashMap<>();
        descriptors = List.of(
                descriptor(MapperContext::getRelatedTwinClassMap, TwinClassEntity.class, RelatedObjectsDTOv1::setTwinClassMap),
                descriptor(MapperContext::getRelatedTwinMap, TwinEntity.class, RelatedObjectsDTOv1::setTwinMap),
                descriptor(MapperContext::getRelatedTwinStatusMap, TwinStatusEntity.class, RelatedObjectsDTOv1::setStatusMap),
                descriptor(MapperContext::getRelatedLinkMap, LinkEntity.class, RelatedObjectsDTOv1::setLinkMap),
                descriptor(MapperContext::getRelatedTwinTriggerMap, TwinTriggerEntity.class, RelatedObjectsDTOv1::setTriggerMap),
                descriptor(MapperContext::getRelatedUserMap, UserEntity.class, RelatedObjectsDTOv1::setUserMap),
                descriptor(MapperContext::getRelatedUserGroupMap, UserGroupEntity.class, RelatedObjectsDTOv1::setUserGroupMap),
                descriptor(MapperContext::getRelatedTwinflowTransitionMap, TwinflowTransitionEntity.class, RelatedObjectsDTOv1::setTransitionsMap),
                descriptor(MapperContext::getRelatedDataListMap, DataListEntity.class, RelatedObjectsDTOv1::setDataListsMap),
                descriptor(MapperContext::getRelatedDataListOptionMap, DataListOptionEntity.class, RelatedObjectsDTOv1::setDataListsOptionMap),
                descriptor(MapperContext::getRelatedSpaceRoleMap, SpaceRoleEntity.class, RelatedObjectsDTOv1::setSpaceRoleMap),
                descriptor(MapperContext::getRelatedBusinessAccountMap, BusinessAccountEntity.class, RelatedObjectsDTOv1::setBusinessAccountMap),
                descriptor(MapperContext::getRelatedPermissionGroupMap, PermissionGroupEntity.class, RelatedObjectsDTOv1::setPermissionGroupMap),
                descriptor(MapperContext::getRelatedPermissionMap, PermissionEntity.class, RelatedObjectsDTOv1::setPermissionMap),
                descriptor(MapperContext::getRelatedPermissionSchemaMap, PermissionSchemaEntity.class, RelatedObjectsDTOv1::setPermissionSchemaMap),
                descriptor(MapperContext::getRelatedTwinflowMap, TwinflowEntity.class, RelatedObjectsDTOv1::setTwinflowMap),
                descriptor(MapperContext::getRelatedTwinflowFactoryMap, TwinflowFactoryEntity.class, RelatedObjectsDTOv1::setTwinflowFactoryMap),
                descriptor(MapperContext::getRelatedFactoryMap, TwinFactoryEntity.class, RelatedObjectsDTOv1::setFactoryMap),
                descriptor(MapperContext::getRelatedFactoryPipelineMap, TwinFactoryPipelineEntity.class, RelatedObjectsDTOv1::setFactoryPipelineMap),
                descriptor(MapperContext::getRelatedFactoryConditionSetMap, TwinFactoryConditionSetEntity.class, RelatedObjectsDTOv1::setFactoryConditionSetMap),
                descriptor(MapperContext::getRelatedFactoryMultiplierMap, TwinFactoryMultiplierEntity.class, RelatedObjectsDTOv1::setFactoryMultiplierMap),
                descriptor(MapperContext::getRelatedFactoryBranchMap, TwinFactoryBranchEntity.class, RelatedObjectsDTOv1::setFactoryBranchMap),
                descriptor(MapperContext::getRelatedFactoryPipelineStepMap, TwinFactoryPipelineStepEntity.class, RelatedObjectsDTOv1::setFactoryPipelineStepMap),
                descriptor(MapperContext::getRelatedFactoryMultiplierFilterMap, TwinFactoryMultiplierFilterEntity.class, RelatedObjectsDTOv1::setFactoryMultiplierFilterMap),
                descriptor(MapperContext::getRelatedFactoryEraserMap, TwinFactoryEraserEntity.class, RelatedObjectsDTOv1::setFactoryEraserMap),
                descriptor(MapperContext::getRelatedFactoryTriggerMap, TwinFactoryTriggerEntity.class, RelatedObjectsDTOv1::setFactoryTriggerMap),
                descriptor(MapperContext::getRelatedFactoryConditionMap, TwinFactoryConditionEntity.class, RelatedObjectsDTOv1::setFactoryConditionMap),
                descriptor(MapperContext::getRelatedFaceMap, FaceEntity.class, RelatedObjectsDTOv1::setFaceMap),
                descriptor(MapperContext::getRelatedCommentMap, TwinCommentEntity.class, RelatedObjectsDTOv1::setCommentMap),
                descriptor(MapperContext::getRelatedFeaturerMap, entityRestMapperRegistry.getFeaturerRestDTOMapper(), FeaturerEntity::getId, featurerMap, RelatedObjectsDTOv1::setFeaturerMap),
                descriptor(MapperContext::getRelatedFeaturerTypeMap, entityRestMapperRegistry.getFeaturerTypeRestDTOMapper(), FeaturerTypeEntity::getId, RelatedObjectsDTOv1::setFeaturerTypeMap), // Integer id: no EntitySecureFindServiceImpl service to derive from
                descriptor(MapperContext::getRelatedTwinClassFieldMap, TwinClassFieldEntity.class, RelatedObjectsDTOv1::setTwinClassFieldMap),
                descriptor(MapperContext::getRelatedTwinClassSchemaMap, TwinClassSchemaEntity.class, RelatedObjectsDTOv1::setTwinClassSchemaMap),
                descriptor(MapperContext::getRelatedTwinflowSchemaMap, TwinflowSchemaEntity.class, RelatedObjectsDTOv1::setTwinflowSchemaMap),
                descriptor(MapperContext::getRelatedTierMap, TierEntity.class, RelatedObjectsDTOv1::setTierMap),
                descriptor(MapperContext::getRelatedAttachmentRestrictionMap, TwinAttachmentRestrictionEntity.class, RelatedObjectsDTOv1::setAttachmentRestrictionMap),
                descriptor(MapperContext::getRelatedTwinClassFreezeMap, TwinClassFreezeEntity.class, RelatedObjectsDTOv1::setTwinClassFreezeMap),
                descriptor(MapperContext::getRelatedClassFieldRuleMap, TwinClassFieldRuleEntity.class, RelatedObjectsDTOv1::setFieldRuleMap),
                descriptor(MapperContext::getRelatedProjectionTypeGroupMap, ProjectionTypeGroupEntity.class, RelatedObjectsDTOv1::setProjectionTypeGroupMap),
                descriptor(MapperContext::getRelatedProjectionTypeMap, ProjectionTypeEntity.class, RelatedObjectsDTOv1::setProjectionTypeMap),
                descriptor(MapperContext::getRelatedSchedulerMap, SchedulerEntity.class, RelatedObjectsDTOv1::setSchedulerMap),
                descriptor(MapperContext::getRelatedHistoryNotificationRecipientMap, HistoryNotificationRecipientEntity.class, RelatedObjectsDTOv1::setHistoryNotificationRecipientMap),
                descriptor(MapperContext::getRelatedNotificationSchemaMap, NotificationSchemaEntity.class, RelatedObjectsDTOv1::setNotificationSchemaMap),
                descriptor(MapperContext::getRelatedNotificationChannelMap, NotificationChannelEntity.class, RelatedObjectsDTOv1::setNotificationChannelMap),
                descriptor(MapperContext::getRelatedNotificationContextMap, NotificationContextEntity.class, RelatedObjectsDTOv1::setNotificationContextMap),
                descriptor(MapperContext::getRelatedNotificationChannelEventMap, NotificationChannelEventEntity.class, RelatedObjectsDTOv1::setNotificationChannelEventMap),
                descriptor(MapperContext::getRelatedTwinValidatorSetMap, TwinValidatorSetEntity.class, RelatedObjectsDTOv1::setTwinValidatorSetMap),
                descriptor(MapperContext::getRelatedHistoryTypeMap, entityRestMapperRegistry.getHistoryTypeRestDTOMapper(), HistoryTypeEntity::getId, RelatedObjectsDTOv1::setHistoryTypeMap), // String id: does not fit Function<T, UUID>
                descriptor(MapperContext::getRelatedActionRestrictionReasonMap, ActionRestrictionReasonEntity.class, RelatedObjectsDTOv1::setActionRestrictionReasonMap),
                descriptor(MapperContext::getRelatedTwinPointerMap, TwinPointerEntity.class, RelatedObjectsDTOv1::setTwinPointerMap),
                descriptor(MapperContext::getRelatedDataListSubsetMap, DataListSubsetEntity.class, RelatedObjectsDTOv1::setDataListSubsetMap),
                descriptor(MapperContext::getRelatedFeaturerParamsMap, entityRestMapperRegistry.getFeaturerParametrizedRestDTOMapper(), FeaturerParams::getFeaturerId, featurerMap, null) // shares the featurerMap accumulator
        );
    }

    /**
     * One related map declaration: where to read postponed objects from, how to convert them (mapper +
     * id getter) and where to put the results. Source and destination key types are separate: e.g. the
     * featurer params map is keyed by a cache string on MapperContext but contributes to featurerMap
     * keyed by the featurer id. {@code destinationSetter} is null for descriptors sharing an accumulated
     * map with the owning descriptor (featurerMap collects both FeaturerEntity and FeaturerParams conversions).
     */
    private record RelatedMapDescriptor<E, SK, DK, D>(
            Function<MapperContext, Map<SK, RelatedObject<E>>> sourceMapGetter,
            RestSimpleDTOMapper<E, ? extends D> mapper,
            Function<? super E, ? extends DK> idGetter,
            Map<DK, D> accumulatedMap,
            BiConsumer<RelatedObjectsDTOv1, Map<DK, D>> destinationSetter) {

        @SuppressWarnings("unchecked")
        private void drain(MapperContext sourceContext, MapperContext mapperContext) throws Exception {
            Map<SK, RelatedObject<E>> source = sourceMapGetter.apply(sourceContext);
            if (source.isEmpty())
                return;
            Map<Object, Object> accumulated = (Map<Object, Object>) accumulatedMap;
            Function<Object, Object> id = (Function<Object, Object>) idGetter;
            RestSimpleDTOMapper<Object, Object> convertingMapper = (RestSimpleDTOMapper<Object, Object>) mapper;
            for (RelatedObject<E> relatedObject : source.values())
                accumulated.put(id.apply(relatedObject.getObject()),
                        convertingMapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
        }

        private void applyResult(RelatedObjectsDTOv1 ret) {
            if (destinationSetter != null)
                destinationSetter.accept(ret, accumulatedMap.isEmpty() ? null : accumulatedMap);
        }
    }

    private static <E, SK, DK, D> RelatedMapDescriptor<E, SK, DK, D> descriptor(
            Function<MapperContext, Map<SK, RelatedObject<E>>> sourceMapGetter,
            RestSimpleDTOMapper<E, ? extends D> mapper,
            Function<? super E, ? extends DK> idGetter,
            BiConsumer<RelatedObjectsDTOv1, Map<DK, D>> destinationSetter) {
        return new RelatedMapDescriptor<>(sourceMapGetter, mapper, idGetter, new HashMap<>(), destinationSetter);
    }

    /** Overload for descriptors sharing an accumulated map with another descriptor (see featurerMap). */
    private static <E, SK, DK, D> RelatedMapDescriptor<E, SK, DK, D> descriptor(
            Function<MapperContext, Map<SK, RelatedObject<E>>> sourceMapGetter,
            RestSimpleDTOMapper<E, ? extends D> mapper,
            Function<? super E, ? extends DK> idGetter,
            Map<DK, D> accumulatedMap,
            BiConsumer<RelatedObjectsDTOv1, Map<DK, D>> destinationSetter) {
        return new RelatedMapDescriptor<>(sourceMapGetter, mapper, idGetter, accumulatedMap, destinationSetter);
    }

    /**
     * Overload deriving the mapper (EntityRestMapperRegistry) and the id getter (EntityServiceRegistry,
     * entityGetIdFunction) by entity class — the regular case for UUID-keyed entity maps. Fails fast at
     * startup when a registry entry is missing. Not applicable to the featurer family (Integer ids,
     * FeaturerParams is not an entity) and historyType (String id): those use the explicit overloads.
     */
    @SuppressWarnings("unchecked")
    private <E, D> RelatedMapDescriptor<E, UUID, UUID, D> descriptor(
            Function<MapperContext, Map<UUID, RelatedObject<E>>> sourceMapGetter,
            Class<E> entityClass,
            BiConsumer<RelatedObjectsDTOv1, Map<UUID, D>> destinationSetter) {
        RestSimpleDTOMapper<E, ?> mapper = (RestSimpleDTOMapper<E, ?>) entityRestMapperRegistry.getMapper(entityClass);
        if (mapper == null)
            throw new IllegalStateException("RelatedObjectsRestDTOConverter: entity class[" + entityClass.getName()
                    + "] has no mapper registered in EntityRestMapperRegistry");
        EntitySecureFindServiceImpl<E> service = (EntitySecureFindServiceImpl<E>) entityServiceRegistry.getService(entityClass);
        if (service == null)
            throw new IllegalStateException("RelatedObjectsRestDTOConverter: entity class[" + entityClass.getName()
                    + "] has no service registered in EntityServiceRegistry");
        return new RelatedMapDescriptor<E, UUID, UUID, D>(sourceMapGetter, (RestSimpleDTOMapper<E, ? extends D>) mapper,
                service.entityGetIdFunction(), new HashMap<>(), destinationSetter);
    }

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        //resolve featurer param entity refs postponed during the main conversion: rendered on level 1
        entityRefRestDTOMapper.resolve(mapperContext);
        //run mappers one more time, because related objects can also contain relations (they were added to the isolated context on the previous step)
        MapperContext mapperContextLevel2 = mapperContext.cloneIgnoreRelatedObjects();
        drain(mapperContext, mapperContextLevel2);
        MapperContext mapperContextLevel3 = mapperContextLevel2.cloneIgnoreRelatedObjects();
        //resolve entity refs postponed during level 1 conversions: rendered on level 2
        entityRefRestDTOMapper.resolve(mapperContextLevel2);
        drain(mapperContextLevel2, mapperContextLevel3);
        //this level was added because of dataLists. In case of search twins, twinClass will be detected on level1, twinClass.tagDataList on level2 and its options only on level3
        mapperContextLevel3.setLazyRelations(true); // on such depth we will not collect related objects anymore
        //resolve entity refs postponed during level 2 conversions: loaded and rendered on this level, the cascade
        //stops here because postpone is a no-op with lazyRelations=true
        entityRefRestDTOMapper.resolve(mapperContextLevel3);
        drain(mapperContextLevel3, mapperContextLevel3);
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        for (RelatedMapDescriptor<?, ?, ?, ?> descriptor : descriptors)
            descriptor.applyResult(ret);
        return ret;
    }

    private void drain(MapperContext sourceContext, MapperContext mapperContext) throws Exception {
        for (RelatedMapDescriptor<?, ?, ?, ?> descriptor : descriptors)
            descriptor.drain(sourceContext, mapperContext);
    }
}
