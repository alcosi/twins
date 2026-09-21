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
 * Declarative: one {@link RelatedMapDescriptor} per related map — a destination setter plus one or
 * more {@link RelatedMapDescriptor.Source sources} of postponed objects — and a single drain loop
 * running on each of the three conversion levels — related objects of level N postpone their own
 * relations into the context of level N+1 (see the inline comments about the levels).
 * EntityRefs postponed by featurer params resolution are drained between the levels by EntityRefRestDTOMapper.
 */
@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {

    private final EntityRestMapperRegistry entityRestMapperRegistry;
    private final EntityServiceRegistry entityServiceRegistry;
    private final EntityRefRestDTOMapper entityRefRestDTOMapper;

    private List<RelatedMapDescriptor<?, ?>> descriptors;

    @PostConstruct
    void initDescriptors() {
        descriptors = List.of(
                descriptor(RelatedObjectsDTOv1::setTwinClassMap, source(TwinClassEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinMap, source(TwinEntity.class)),
                descriptor(RelatedObjectsDTOv1::setStatusMap, source(TwinStatusEntity.class)),
                descriptor(RelatedObjectsDTOv1::setLinkMap, source(LinkEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTriggerMap, source(TwinTriggerEntity.class)),
                descriptor(RelatedObjectsDTOv1::setUserMap, source(UserEntity.class)),
                descriptor(RelatedObjectsDTOv1::setUserGroupMap, source(UserGroupEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTransitionsMap, source(TwinflowTransitionEntity.class)),
                descriptor(RelatedObjectsDTOv1::setDataListsMap, source(DataListEntity.class)),
                descriptor(RelatedObjectsDTOv1::setDataListsOptionMap, source(DataListOptionEntity.class)),
                descriptor(RelatedObjectsDTOv1::setSpaceRoleMap, source(SpaceRoleEntity.class)),
                descriptor(RelatedObjectsDTOv1::setBusinessAccountMap, source(BusinessAccountEntity.class)),
                descriptor(RelatedObjectsDTOv1::setPermissionGroupMap, source(PermissionGroupEntity.class)),
                descriptor(RelatedObjectsDTOv1::setPermissionMap, source(PermissionEntity.class)),
                descriptor(RelatedObjectsDTOv1::setPermissionSchemaMap, source(PermissionSchemaEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinflowMap, source(TwinflowEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinflowFactoryMap, source(TwinflowFactoryEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryMap, source(TwinFactoryEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryPipelineMap, source(TwinFactoryPipelineEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryConditionSetMap, source(TwinFactoryConditionSetEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryMultiplierMap, source(TwinFactoryMultiplierEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryBranchMap, source(TwinFactoryBranchEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryPipelineStepMap, source(TwinFactoryPipelineStepEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryMultiplierFilterMap, source(TwinFactoryMultiplierFilterEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryEraserMap, source(TwinFactoryEraserEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryTriggerMap, source(TwinFactoryTriggerEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFactoryConditionMap, source(TwinFactoryConditionEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFaceMap, source(FaceEntity.class)),
                descriptor(RelatedObjectsDTOv1::setCommentMap, source(TwinCommentEntity.class)),
                descriptor(RelatedObjectsDTOv1::setI18nMap, source(I18nEntity.class)),
                //the featurer map is drained from two sources: featurer definitions postponed as FeaturerEntity
                //and featurer+params pairs postponed as FeaturerParams (both keyed by the featurer id in the result)
                descriptor(RelatedObjectsDTOv1::setFeaturerMap,
                        source(FeaturerEntity.class, entityRestMapperRegistry.getFeaturerRestDTOMapper(), FeaturerEntity::getId),
                        source(FeaturerParams.class, entityRestMapperRegistry.getFeaturerParametrizedRestDTOMapper(), FeaturerParams::getFeaturerId)),
                // Integer id: no EntitySecureFindServiceImpl service to derive the id getter from
                descriptor(RelatedObjectsDTOv1::setFeaturerTypeMap, source(FeaturerTypeEntity.class, entityRestMapperRegistry.getFeaturerTypeRestDTOMapper(), FeaturerTypeEntity::getId)),
                descriptor(RelatedObjectsDTOv1::setTwinClassFieldMap, source(TwinClassFieldEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinClassSchemaMap, source(TwinClassSchemaEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinflowSchemaMap, source(TwinflowSchemaEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTierMap, source(TierEntity.class)),
                descriptor(RelatedObjectsDTOv1::setAttachmentRestrictionMap, source(TwinAttachmentRestrictionEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinClassFreezeMap, source(TwinClassFreezeEntity.class)),
                descriptor(RelatedObjectsDTOv1::setFieldRuleMap, source(TwinClassFieldRuleEntity.class)),
                descriptor(RelatedObjectsDTOv1::setProjectionTypeGroupMap, source(ProjectionTypeGroupEntity.class)),
                descriptor(RelatedObjectsDTOv1::setProjectionTypeMap, source(ProjectionTypeEntity.class)),
                descriptor(RelatedObjectsDTOv1::setSchedulerMap, source(SchedulerEntity.class)),
                descriptor(RelatedObjectsDTOv1::setHistoryNotificationRecipientMap, source(HistoryNotificationRecipientEntity.class)),
                descriptor(RelatedObjectsDTOv1::setNotificationSchemaMap, source(NotificationSchemaEntity.class)),
                descriptor(RelatedObjectsDTOv1::setNotificationChannelMap, source(NotificationChannelEntity.class)),
                descriptor(RelatedObjectsDTOv1::setNotificationContextMap, source(NotificationContextEntity.class)),
                descriptor(RelatedObjectsDTOv1::setNotificationChannelEventMap, source(NotificationChannelEventEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinValidatorSetMap, source(TwinValidatorSetEntity.class)),
                // String id: does not fit Function<T, UUID>
                descriptor(RelatedObjectsDTOv1::setHistoryTypeMap, source(HistoryTypeEntity.class, entityRestMapperRegistry.getHistoryTypeRestDTOMapper(), HistoryTypeEntity::getId)),
                descriptor(RelatedObjectsDTOv1::setActionRestrictionReasonMap, source(ActionRestrictionReasonEntity.class)),
                descriptor(RelatedObjectsDTOv1::setTwinPointerMap, source(TwinPointerEntity.class)),
                descriptor(RelatedObjectsDTOv1::setDataListSubsetMap, source(DataListSubsetEntity.class))
        );
    }

    /**
     * One related map declaration: the destination setter and the sources of postponed objects drained
     * into the shared result map. Most maps have a single source; the featurer map drains two
     * (FeaturerEntity definitions and FeaturerParams pairs), both keyed by the featurer id in the result.
     * Source and destination key types are separate: e.g. the featurer params source is keyed by a
     * cache string on MapperContext but contributes to featurerMap keyed by the featurer id.
     * Stateless by design: the accumulator map is created per convert() call — the converter is a
     * singleton bean, accumulators must never survive a request.
     */
    private record RelatedMapDescriptor<DK, D>(
            List<Source<?, DK, D>> sources,
            BiConsumer<RelatedObjectsDTOv1, Map<DK, D>> destinationSetter) {

        /** One postponed-objects source: where to read from, how to convert and how to key the results. */
        private record Source<E, DK, D>(
                Class<E> relatedClass,
                RestSimpleDTOMapper<E, ? extends D> mapper,
                Function<? super E, ? extends DK> idGetter) {
        }

        @SuppressWarnings("unchecked")
        private void drain(MapperContext sourceContext, MapperContext mapperContext, Map<Object, Object> accumulated) throws Exception {
            for (Source<?, DK, D> source : sources)
                drainSource(sourceContext, mapperContext, accumulated, source.relatedClass(),
                        (Function<Object, Object>) source.idGetter(), (RestSimpleDTOMapper<Object, Object>) source.mapper());
        }

        private static void drainSource(MapperContext sourceContext, MapperContext mapperContext, Map<Object, Object> accumulated,
                                        Class<?> relatedClass, Function<Object, Object> id, RestSimpleDTOMapper<Object, Object> convertingMapper) throws Exception {
            Map<Object, RelatedObject<Object>> source = (Map<Object, RelatedObject<Object>>) (Map<?, ?>) sourceContext.getRelatedMap(relatedClass);
            if (source == null || source.isEmpty())
                return;
            for (RelatedObject<Object> relatedObject : source.values())
                accumulated.put(id.apply(relatedObject.getObject()),
                        convertingMapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
        }

        @SuppressWarnings("unchecked")
        private void applyResult(RelatedObjectsDTOv1 ret, Map<Object, Object> accumulated) {
            ((BiConsumer<RelatedObjectsDTOv1, Map<Object, Object>>) (BiConsumer<?, ?>) destinationSetter)
                    .accept(ret, accumulated.isEmpty() ? null : accumulated);
        }
    }

    @SafeVarargs
    private static <DK, D> RelatedMapDescriptor<DK, D> descriptor(
            BiConsumer<RelatedObjectsDTOv1, Map<DK, D>> destinationSetter,
            RelatedMapDescriptor.Source<?, DK, D>... sources) {
        return new RelatedMapDescriptor<>(List.of(sources), destinationSetter);
    }

    /**
     * Source deriving the mapper (EntityRestMapperRegistry) and the id getter (EntityServiceRegistry,
     * entityGetIdFunction) by entity class — the regular case for UUID-keyed entity maps. Fails fast at
     * startup when a registry entry is missing. Not applicable to the featurer family (Integer ids,
     * FeaturerParams is not an entity) and historyType (String id): those use the explicit overload below.
     */
    @SuppressWarnings("unchecked")
    private <E, D> RelatedMapDescriptor.Source<E, UUID, D> source(Class<E> entityClass) {
        RestSimpleDTOMapper<E, ?> mapper = (RestSimpleDTOMapper<E, ?>) entityRestMapperRegistry.getMapper(entityClass);
        if (mapper == null)
            throw new IllegalStateException("RelatedObjectsRestDTOConverter: entity class[" + entityClass.getName()
                    + "] has no mapper registered in EntityRestMapperRegistry");
        EntitySecureFindServiceImpl<E> service = (EntitySecureFindServiceImpl<E>) entityServiceRegistry.getService(entityClass);
        if (service == null)
            throw new IllegalStateException("RelatedObjectsRestDTOConverter: entity class[" + entityClass.getName()
                    + "] has no service registered in EntityServiceRegistry");
        return new RelatedMapDescriptor.Source<>(entityClass, (RestSimpleDTOMapper<E, ? extends D>) mapper,
                service.entityGetIdFunction());
    }

    private static <E, DK, D> RelatedMapDescriptor.Source<E, DK, D> source(
            Class<E> relatedClass,
            RestSimpleDTOMapper<E, ? extends D> mapper,
            Function<? super E, ? extends DK> idGetter) {
        return new RelatedMapDescriptor.Source<>(relatedClass, mapper, idGetter);
    }

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        //per-convert accumulators, one per descriptor: the descriptors are a shared singleton config,
        //the maps must die with the request (no data leaking between requests/tenants)
        Map<RelatedMapDescriptor<?, ?>, Map<Object, Object>> accumulators = new HashMap<>();
        for (RelatedMapDescriptor<?, ?> descriptor : descriptors)
            accumulators.put(descriptor, new HashMap<>());
        //resolve featurer param entity refs postponed during the main conversion: rendered on level 1
        entityRefRestDTOMapper.resolve(mapperContext);
        //run mappers one more time, because related objects can also contain relations (they were added to the isolated context on the previous step)
        MapperContext mapperContextLevel2 = mapperContext.cloneIgnoreRelatedObjects();
        drain(mapperContext, mapperContextLevel2, accumulators);
        MapperContext mapperContextLevel3 = mapperContextLevel2.cloneIgnoreRelatedObjects();
        //resolve entity refs postponed during level 1 conversions: rendered on level 2
        entityRefRestDTOMapper.resolve(mapperContextLevel2);
        drain(mapperContextLevel2, mapperContextLevel3, accumulators);
        //this level was added because of dataLists. In case of search twins, twinClass will be detected on level1, twinClass.tagDataList on level2 and its options only on level3
        mapperContextLevel3.setLazyRelations(true); // on such depth we will not collect related objects anymore
        //resolve entity refs postponed during level 2 conversions: loaded and rendered on this level, the cascade
        //stops here because postpone is a no-op with lazyRelations=true
        entityRefRestDTOMapper.resolve(mapperContextLevel3);
        drain(mapperContextLevel3, mapperContextLevel3, accumulators);
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        for (RelatedMapDescriptor<?, ?> descriptor : descriptors)
            descriptor.applyResult(ret, accumulators.get(descriptor));
        return ret;
    }

    private void drain(MapperContext sourceContext, MapperContext mapperContext, Map<RelatedMapDescriptor<?, ?>, Map<Object, Object>> accumulators) throws Exception {
        for (RelatedMapDescriptor<?, ?> descriptor : descriptors)
            descriptor.drain(sourceContext, mapperContext, accumulators.get(descriptor));
    }
}
