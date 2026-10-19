package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.mappercontext.EntityRef;

import java.util.*;

/**
 * Registry of ALL EntitySecureFindServiceImpl beans by their entity class (resolved from the generic
 * superclass): knows how to load entities of a given class (findEntitiesSafe — strict, throws on
 * missing/deleted/denied references). Used by the mapping layer (EntityRefRestDTOMapper) to resolve
 * lazy EntityRefs on the drain phase. New EntitySecureFindServiceImpl services register themselves
 * automatically; the entity still needs a mapper (+ show mode) in EntityRestMapperRegistry to be rendered.
 */
@Component
@Slf4j
public class EntityServiceRegistry {

    private final Map<Class<?>, EntitySecureFindServiceImpl<?>> registry = new HashMap<>();

    public EntityServiceRegistry(List<EntitySecureFindServiceImpl<?>> secureFindServices) {
        for (EntitySecureFindServiceImpl<?> service : secureFindServices) {
            Class<?>[] entityTypeArguments = GenericTypeResolver.resolveTypeArguments(service.getClass(), EntitySecureFindServiceImpl.class);
            if (entityTypeArguments == null || entityTypeArguments.length == 0) {
                log.error("Can not resolve the entity type of service[{}]", service.getClass().getName());
                continue;
            }
            register(entityTypeArguments[0], service);
        }
    }

    private void register(Class<?> entityClass, EntitySecureFindServiceImpl<?> service) {
        EntitySecureFindServiceImpl<?> previous = registry.put(entityClass, service);
        if (previous != null)
            log.error("Duplicate EntityServiceRegistry entry for entity class[{}]: [{}] replaced by [{}]",
                    entityClass.getName(), previous.getClass().getSimpleName(), service.getClass().getSimpleName());
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
        load(List.of(src));
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
