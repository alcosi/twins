package org.twins.core.mappers.rest.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.related.EntityRestMapperRegistry;
import org.twins.core.service.EntityServiceRegistry;

import java.util.*;

/**
 * Resolves lazy {@link EntityRef} objects (not-yet-loaded entities referenced by featurer params) following
 * the standard load pattern: beforeCollectionConversion filters refs with no loaded entity (needLoad),
 * groups them by entity class and runs ONE findEntitiesSafe query per class via {@link EntityServiceRegistry}
 * (no N+1), then distributes the loaded entities back into the refs.
 * RelatedObjectsRestDTOConverter calls {@link #resolve(MapperContext)} between conversion levels, when all
 * refs of the level are collected: it runs the bulk load and postpones the loaded entities into their typed
 * relatedXxxMap at the default show mode of their mapper (DETAILED where available — see
 * EntityRestMapperRegistry.getShowMode), so they are rendered by the next conversion level.
 * {@link #convert(EntityRef, MapperContext)} resolves a single ref (one findEntitiesSafe call, REQUEST-cached)
 * and converts the loaded entity into its DTO via the mapper resolved from EntityRestMapperRegistry by entity class.
 * Fail fast by design: findEntitiesSafe throws ServiceException on a missing/deleted reference or read denial.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityRefRestDTOMapper extends RestSimpleDTOMapper<EntityRef, Object> {

    private final EntityServiceRegistry entityServiceRegistry;

    //lazy: the registry constructor-injects mappers that depend on this mapper through FeaturerParametrizedRestDTOMapper
    @Lazy
    @Autowired
    private EntityRestMapperRegistry entityRestMapperRegistry;

    /**
     * Pattern load: bulk-resolves refs with no loaded entity, grouped by entity class — one
     * findEntitiesSafe query per class — and distributes the loaded entities back into the refs.
     */
    @Override
    public void beforeCollectionConversion(Collection<EntityRef> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (srcCollection == null || srcCollection.isEmpty())
            return;
        entityServiceRegistry.load(srcCollection);
    }

    /**
     * Single-ref resolution: loads the referenced entity (one findEntitiesSafe call, REQUEST-cached) into
     * the ref and converts it into its DTO via the mapper resolved from RestDTOMapperRegistry by entity class.
     */
    @Override
    public Object convert(EntityRef src, MapperContext mapperContext) throws Exception {
        entityServiceRegistry.load(src);
        RestSimpleDTOMapper<Object, ?> entityMapper = mapperForEntity(src.getEntityClass());
        if (entityMapper == null)
            return null;
        return entityMapper.convert(src.getEntity(), mapperContext);
    }

    /**
     * Drain invoked by RelatedObjectsRestDTOConverter between conversion levels: bulk-loads all postponed
     * refs of the given context and postpones the loaded entities into their typed related maps.
     */
    public void resolve(MapperContext mapperContext) throws ServiceException {
        Map<Object, RelatedObject<EntityRef>> relatedEntityRefMap = mapperContext.getRelatedMap(EntityRef.class);
        if (relatedEntityRefMap == null || relatedEntityRefMap.isEmpty())
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
                MapperContext newFork = mapperContext.fork();
                MapperMode showMode = entityRestMapperRegistry.getShowMode(entityClass);
                if (showMode != null)
                    newFork.setPriorityMinMode(showMode); // default DETAILED; a mode explicitly requested by the client keeps priority
                return newFork;
            });
            fork.addRelatedObject(entity);
        }
    }

    @SuppressWarnings("unchecked")
    private RestSimpleDTOMapper<Object, ?> mapperForEntity(Class<?> entityClass) {
        RestSimpleDTOMapper<?, ?> mapper = entityRestMapperRegistry.getMapper(entityClass);
        if (mapper == null) {
            log.error("Entity class[{}] has no registered RestSimpleDTOMapper", entityClass.getName());
            return null;
        }
        return (RestSimpleDTOMapper<Object, ?>) mapper;
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
