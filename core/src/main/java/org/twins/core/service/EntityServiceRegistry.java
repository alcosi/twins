package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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
import org.twins.core.mappers.rest.mappercontext.EntityRef;
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
 * Registry of EntitySecureFindServiceImpl services by entity class: knows how to load entities
 * of a given class (findEntitiesSafe — strict, throws on missing/deleted/denied references).
 * Used by the mapping layer (EntityRefRestDTOMapper) to resolve lazy EntityRefs on the drain phase.
 * A new resolvable entity type needs a register() line here (loader) and a register() line in
 * RestDTOMapperRegistry (mapper + short show mode).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityServiceRegistry {

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

    private final Map<Class<?>, EntitySecureFindServiceImpl<?>> registry = new HashMap<>();

    @PostConstruct
    void initRegistry() {
        register(TwinClassEntity.class, twinClassService);
        register(TwinClassFieldEntity.class, twinClassFieldService);
        register(TwinClassSchemaEntity.class, twinClassSchemaService);
        register(TwinflowSchemaEntity.class, twinflowSchemaService);
        register(TwinEntity.class, twinService);
        register(TwinStatusEntity.class, twinStatusService);
        register(LinkEntity.class, linkService);
        register(DataListEntity.class, dataListService);
        register(DataListOptionEntity.class, dataListOptionService);
        register(DataListSubsetEntity.class, dataListSubsetService);
        register(TwinPointerEntity.class, twinPointerService);
        register(PermissionEntity.class, permissionService);
        register(PermissionSchemaEntity.class, permissionSchemaService);
        register(I18nEntity.class, i18nService);
        register(TwinAttachmentRestrictionEntity.class, attachmentRestrictionService);
        register(UserGroupEntity.class, userGroupService);
        register(UserEntity.class, userService);
        register(ProjectionTypeGroupEntity.class, projectionTypeGroupService);
        register(TwinClassFreezeEntity.class, twinClassFreezeService);
    }

    private void register(Class<?> entityClass, EntitySecureFindServiceImpl<?> service) {
        EntitySecureFindServiceImpl<?> previous = registry.put(entityClass, service);
        if (previous != null)
            log.error("Duplicate EntityRegistry entry for entity class[{}]", entityClass.getName());
    }

    public EntitySecureFindServiceImpl<?> getService(Class<?> entityClass) {
        return registry.get(entityClass);
    }

    /**
     * Bulk-loads entities of the given class by ids. Returns null if the class is not registered.
     */
    public Kit<?, UUID> findEntitiesSafe(Class<?> entityClass, Collection<UUID> ids) throws ServiceException {
        EntitySecureFindServiceImpl<?> service = getService(entityClass);
        if (service == null) {
            log.error("Entity class[{}] is not registered in EntityServiceRegistry", entityClass.getName());
            return null;
        }
        return service.findEntitiesSafe(ids);
    }

    public void load(EntityRef src) throws ServiceException {
        load(Collections.singletonList(src));
    }

    public void load(Collection<EntityRef> srcCollection) throws ServiceException {
        Map<Class<?>, Kit<EntityRef, UUID>> needLoadIds = new LinkedHashMap<>();
        for (EntityRef entityRef : srcCollection) // needLoad: only refs without a loaded entity
            if (entityRef != null && entityRef.getEntity() == null && entityRef.getEntityClass() != null && entityRef.getId() != null)
                needLoadIds.computeIfAbsent(entityRef.getEntityClass(), key -> new Kit<>(EntityRef::getId)).add(entityRef);
        for (var entry : needLoadIds.entrySet()) {
            Kit<?, UUID> loaded = findEntitiesSafe(entry.getKey(), entry.getValue().getIdSet());
            if (loaded == null) // class not registered: already logged by EntityServiceRegistry
                continue;
            for (EntityRef entityRef : entry.getValue()) // distribute loaded entities via setters
                entityRef.setEntity(loaded.get(entityRef.getId()));
        }
    }
}
