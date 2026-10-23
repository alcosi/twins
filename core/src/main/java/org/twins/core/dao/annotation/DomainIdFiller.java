package org.twins.core.dao.annotation;

import org.twins.core.enums.datascope.DataScopeType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal {@code domain_id} autofill for domain-scoped settings rows (TWINS-719).
 *
 * Fills the {@code domainId} field of an entity whose class {@link DataScope} includes
 * {@link DataScopeType#DOMAIN}, but only:
 * <ul>
 *   <li>on insert — the entity id is still unassigned (ids are generated at persist time),
 *       so already-loaded rows being updated are never touched (a system row must not turn
 *       into a domain-owned one on update);</li>
 *   <li>when no explicit value is set — import / duplicate / rollback paths provide their own;</li>
 * </ul>
 * System rows are not created through services, so a null {@code domainId} on insert means
 * "current user's domain". Tables scoped via a parent FK (no own {@code domainId} field yet)
 * are skipped silently — their versioning wave adds the column and the field together.
 */
public final class DomainIdFiller {

    private DomainIdFiller() {
    }

    private record FillableFields(Field id, Field domainId) {

        private static FillableFields of(Class<?> entityClass) throws NoSuchFieldException {
            Field id = entityClass.getDeclaredField("id");
            Field domainId = entityClass.getDeclaredField("domainId");
            id.setAccessible(true);
            domainId.setAccessible(true);
            return new FillableFields(id, domainId);
        }
    }

    private static final Map<Class<?>, Optional<FillableFields>> FILLABLE_FIELDS = new ConcurrentHashMap<>();

    public static <T> T fillOnInsert(T entity, UUID domainId) {
        if (entity == null || domainId == null)
            return entity;
        Optional<FillableFields> fillableFields = FILLABLE_FIELDS.computeIfAbsent(entity.getClass(), DomainIdFiller::detect);
        if (fillableFields.isEmpty())
            return entity;
        try {
            if (fillableFields.get().id().get(entity) != null        // not an insert
                    || fillableFields.get().domainId().get(entity) != null)  // explicit value wins
                return entity;
            fillableFields.get().domainId().set(entity, domainId);
            return entity;
        } catch (IllegalAccessException e) {
            // unreachable after setAccessible — means the entity class shape changed unexpectedly
            throw new IllegalStateException("cannot autofill domainId on " + entity.getClass().getSimpleName(), e);
        }
    }

    public static <T> void fillOnInsert(Iterable<T> entities, UUID domainId) {
        if (entities == null)
            return;
        for (T entity : entities)
            fillOnInsert(entity, domainId);
    }

    private static Optional<FillableFields> detect(Class<?> entityClass) {
        DataScope dataScope = entityClass.getAnnotation(DataScope.class);
        if (dataScope == null || !Arrays.asList(dataScope.value()).contains(DataScopeType.DOMAIN))
            return Optional.empty();
        try {
            return Optional.of(FillableFields.of(entityClass));
        } catch (NoSuchFieldException e) {
            // scoped via parent FK, no own domain_id column/field yet
            return Optional.empty();
        }
    }
}
