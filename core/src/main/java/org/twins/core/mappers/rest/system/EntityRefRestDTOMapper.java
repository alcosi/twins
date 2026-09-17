package org.twins.core.mappers.rest.system;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
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
 * Drain-phase mapper for postponed {@link EntityRef} objects (not-yet-loaded entities referenced by featurer params).
 * EntityRefs are cheap to postpone (in-memory, no DB access) during the mapping phase; this mapper is invoked
 * by RelatedObjectsRestDTOConverter between conversion levels, when all refs of the level are collected,
 * and bulk-loads them grouped by entity class — one findEntitiesSafe query per class (no N+1).
 * Loaded entities are postponed into their typed relatedXxxMap at (at least) SHORT mode, so they are
 * rendered by the next conversion level.
 * EntityRef itself never produces a DTO and is never exposed in relatedObjects.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityRefRestDTOMapper extends RestSimpleDTOMapper<EntityRef, EntityRef> {

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
     * Bulk-loads all postponed EntityRefs of the given context grouped by entity class and postpones the
     * loaded entities into their typed related maps. Fail fast: findEntitiesSafe throws ServiceException
     * on missing/deleted reference or read denial — the endpoint returns an explicit error (by design).
     */
    public void resolve(MapperContext mapperContext) throws ServiceException {
        Map<String, RelatedObject<EntityRef>> relatedEntityRefMap = mapperContext.getRelatedEntityRefMap();
        if (relatedEntityRefMap.isEmpty())
            return;
        Map<Class<?>, Set<UUID>> groupedIds = new LinkedHashMap<>();
        for (RelatedObject<EntityRef> relatedObject : relatedEntityRefMap.values()) {
            EntityRef entityRef = relatedObject.getObject();
            if (entityRef == null || entityRef.getEntityClass() == null || entityRef.getId() == null)
                continue;
            groupedIds.computeIfAbsent(entityRef.getEntityClass(), key -> new LinkedHashSet<>()).add(entityRef.getId());
        }
        for (Map.Entry<Class<?>, Set<UUID>> entry : groupedIds.entrySet()) {
            TargetHandler handler = registry.get(entry.getKey());
            if (handler == null) {
                log.error("EntityRef target class[{}] is not registered in EntityRefRestDTOMapper", entry.getKey().getName());
                continue;
            }
            Collection<?> entities = handler.service().findEntitiesSafe(entry.getValue()).getCollection();
            MapperContext fork = mapperContext.fork();
            if (handler.shortMode() != null)
                fork.setPriorityMinMode(handler.shortMode()); // respects a more detailed mode explicitly requested by the client
            for (Object entity : entities)
                fork.addRelatedObject(entity);
        }
        relatedEntityRefMap.clear();
    }

    @Override
    public void map(EntityRef src, EntityRef dst, MapperContext mapperContext) {
        // EntityRef never produces a DTO: refs are only drained by resolve()
    }

    @Override
    public String getObjectCacheId(EntityRef src) {
        return src == null ? null : src.cacheKey();
    }
}
