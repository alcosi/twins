package org.twins.core.mappers.rest.system;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.related.RestDTOMapperRegistry;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.datalist.DataListSubsetService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.PermissionSchemaService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.projection.ProjectionTypeGroupService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFreezeService;
import org.twins.core.service.twinclass.TwinClassSchemaService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinflow.TwinflowSchemaService;
import org.twins.core.service.twinpointer.TwinPointerService;
import org.twins.core.service.twinstatus.TwinStatusService;
import org.twins.core.service.user.UserService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.*;

/**
 * Resolves lazy {@link EntityRef} objects (not-yet-loaded entities referenced by featurer params) following
 * the standard load pattern: beforeCollectionConversion filters refs with no loaded entity (needLoad),
 * groups them by entity class and runs ONE findEntitiesSafe query per class (no N+1), then distributes
 * the loaded entities back into the refs.
 * RelatedObjectsRestDTOConverter calls {@link #resolve(MapperContext)} between conversion levels, when all
 * refs of the level are collected: it runs the bulk load and postpones the loaded entities into their typed
 * relatedXxxMap at (at least) SHORT mode, so they are rendered by the next conversion level.
 * {@link #convert(EntityRef, MapperContext)} resolves a single ref (one findEntitiesSafe call, REQUEST-cached)
 * and converts the loaded entity into its DTO via the mapper resolved from RestDTOMapperRegistry by entity class.
 * Fail fast by design: findEntitiesSafe throws ServiceException on a missing/deleted reference or read denial.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityRefRestDTOMapper extends RestSimpleDTOMapper<EntityRef, Object> {

    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassSchemaService twinClassSchemaService;
    private final TwinflowSchemaService twinflowSchemaService;
    private final TwinService twinService;
    private final TwinStatusService twinStatusService;
    private final LinkService linkService;
    private final DataListService dataListService;
    private final DataListOptionService dataListOptionService;
    private final DataListSubsetService dataListSubsetService;
    private final TwinPointerService twinPointerService;
    private final PermissionService permissionService;
    private final PermissionSchemaService permissionSchemaService;
    private final I18nService i18nService;
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final UserGroupService userGroupService;
    private final UserService userService;
    private final ProjectionTypeGroupService projectionTypeGroupService;
    private final TwinClassFreezeService twinClassFreezeService;

    //lazy: the registry constructor-injects mappers that depend on this mapper through FeaturerParametrizedRestDTOMapper
    @Lazy
    @Autowired
    private RestDTOMapperRegistry restDTOMapperRegistry;

    private final Map<Class<?>, TargetHandler> registry = new HashMap<>();

    private record TargetHandler(EntitySecureFindServiceImpl<?> service, MapperMode shortMode) {
    }

    @PostConstruct
    void initRegistry() {
        register(TwinClassEntity.class, twinClassService, TwinClassMode.SHORT);
        register(TwinClassFieldEntity.class, twinClassFieldService, TwinClassFieldMode.SHORT);
        register(TwinClassSchemaEntity.class, twinClassSchemaService, TwinClassSchemaMode.SHORT);
        register(TwinflowSchemaEntity.class, twinflowSchemaService, TwinflowSchemaMode.SHORT);
        register(TwinEntity.class, twinService, TwinMode.SHORT);
        register(TwinStatusEntity.class, twinStatusService, StatusMode.SHORT);
        register(LinkEntity.class, linkService, LinkMode.SHORT);
        register(DataListEntity.class, dataListService, DataListMode.SHORT);
        register(DataListOptionEntity.class, dataListOptionService, DataListOptionMode.SHORT);
        register(DataListSubsetEntity.class, dataListSubsetService, DataListSubsetMode.SHORT);
        register(TwinPointerEntity.class, twinPointerService, TwinPointerMode.SHORT);
        register(PermissionEntity.class, permissionService, PermissionMode.SHORT);
        register(PermissionSchemaEntity.class, permissionSchemaService, PermissionSchemaMode.SHORT);
        register(I18nEntity.class, i18nService, I18nMode.SHORT);
        register(TwinAttachmentRestrictionEntity.class, attachmentRestrictionService, null); // mapper has no mode binding
        register(UserGroupEntity.class, userGroupService, UserGroupMode.SHORT);
        register(UserEntity.class, userService, UserMode.SHORT);
        register(ProjectionTypeGroupEntity.class, projectionTypeGroupService, null); // mapper has no mode binding
        register(TwinClassFreezeEntity.class, twinClassFreezeService, TwinClassFreezeMode.SHORT);
    }

    private void register(Class<?> entityClass, EntitySecureFindServiceImpl<?> service, MapperMode shortMode) {
        TargetHandler previous = registry.put(entityClass, new TargetHandler(service, shortMode));
        if (previous != null)
            log.error("Duplicate EntityRef registry entry for class[{}]", entityClass.getName());
    }

    /**
     * Pattern load: bulk-resolves refs with no loaded entity, grouped by entity class — one
     * findEntitiesSafe query per class — and distributes the loaded entities back into the refs.
     */
    @Override
    public void beforeCollectionConversion(Collection<EntityRef> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (srcCollection == null || srcCollection.isEmpty())
            return;
        Map<Class<?>, Set<UUID>> needLoadIds = new LinkedHashMap<>();
        for (EntityRef entityRef : srcCollection) // needLoad: only refs without a loaded entity
            if (entityRef != null && entityRef.getEntity() == null && entityRef.getEntityClass() != null && entityRef.getId() != null)
                needLoadIds.computeIfAbsent(entityRef.getEntityClass(), key -> new LinkedHashSet<>()).add(entityRef.getId());
        for (Map.Entry<Class<?>, Set<UUID>> entry : needLoadIds.entrySet()) {
            TargetHandler handler = registry.get(entry.getKey());
            if (handler == null) {
                log.error("EntityRef target class[{}] is not registered in EntityRefRestDTOMapper", entry.getKey().getName());
                continue;
            }
            Map<UUID, ?> loadedById = handler.service().findEntitiesSafe(entry.getValue()).getMap();
            for (EntityRef entityRef : srcCollection) // distribute loaded entities via setters
                if (entityRef.getEntity() == null && entry.getKey().equals(entityRef.getEntityClass())) {
                    Object entity = loadedById.get(entityRef.getId());
                    if (entity != null) // with ifMissedThrows a miss would have thrown above
                        entityRef.setEntity(entity);
                }
        }
    }

    /**
     * Single-ref resolution: loads the referenced entity (one findEntitiesSafe call, REQUEST-cached) into
     * the ref and converts it into its DTO via the mapper resolved from RestDTOMapperRegistry by entity class.
     */
    @Override
    public Object convert(EntityRef src, MapperContext mapperContext) throws Exception {
        if (src == null)
            return null;
        if (src.getEntity() == null) {
            if (src.getEntityClass() == null || src.getId() == null)
                return null;
            TargetHandler handler = registry.get(src.getEntityClass());
            if (handler == null) {
                log.error("EntityRef target class[{}] is not registered in EntityRefRestDTOMapper", src.getEntityClass().getName());
                return null;
            }
            src.setEntity(handler.service().findEntitiesSafe(Set.of(src.getId())).getMap().get(src.getId()));
        }
        RestSimpleDTOMapper<Object, ?> entityMapper = mapperForEntity(src.getEntityClass());
        if (entityMapper == null)
            return null;
        return entityMapper.convert(src.getEntity(), mapperContext);
    }

    @SuppressWarnings("unchecked")
    private RestSimpleDTOMapper<Object, ?> mapperForEntity(Class<?> entityClass) {
        RestSimpleDTOMapper<?, ?> mapper = restDTOMapperRegistry.getMapper(entityClass);
        if (mapper == null) {
            log.error("Entity class[{}] has no registered RestSimpleDTOMapper", entityClass.getName());
            return null;
        }
        return (RestSimpleDTOMapper<Object, ?>) mapper;
    }

    /**
     * Drain invoked by RelatedObjectsRestDTOConverter between conversion levels: bulk-loads all postponed
     * refs of the given context and postpones the loaded entities into their typed related maps.
     */
    public void resolve(MapperContext mapperContext) throws ServiceException {
        Map<String, RelatedObject<EntityRef>> relatedEntityRefMap = mapperContext.getRelatedEntityRefMap();
        if (relatedEntityRefMap.isEmpty())
            return;
        List<EntityRef> entityRefs = new ArrayList<>(relatedEntityRefMap.size());
        for (RelatedObject<EntityRef> relatedObject : relatedEntityRefMap.values())
            if (relatedObject.getObject() != null)
                entityRefs.add(relatedObject.getObject());
        relatedEntityRefMap.clear();
        beforeCollectionConversion(entityRefs, mapperContext); // pattern load: one bulk query per entity class
        //materialize loaded entities into their typed related maps, grouped by class to reuse the mode fork
        Map<Class<?>, MapperContext> forks = new LinkedHashMap<>();
        for (EntityRef entityRef : entityRefs) {
            Object entity = entityRef.getEntity();
            if (entity == null)
                continue;
            MapperContext fork = forks.computeIfAbsent(entityRef.getEntityClass(), entityClass -> {
                TargetHandler handler = registry.get(entityClass);
                MapperContext newFork = mapperContext.fork();
                if (handler != null && handler.shortMode() != null)
                    newFork.setPriorityMinMode(handler.shortMode()); // respects a more detailed mode explicitly requested by the client
                return newFork;
            });
            fork.addRelatedObject(entity);
        }
    }

    @Override
    public void map(EntityRef src, Object dst, MapperContext mapperContext) {
        // conversion is fully handled by convert(): resolve the ref, then delegate to the entity mapper
    }

    @Override
    public String getObjectCacheId(EntityRef src) {
        return src == null ? null : src.cacheKey();
    }
}
