package org.twins.core.mappers.rest.mappercontext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Lazy reference to an entity used as a featurer param value, following the load pattern
 * (load_method_pattern.md, variant A): the FK id is known, the entity is loaded in bulk later.
 * Postponed into {@link MapperContext} during the mapping phase (cheap, no DB access); EntityRefRestDTOMapper
 * then resolves it on the drain phase via beforeCollectionConversion (one bulk query per entity class)
 * and fills {@link #entity} — exactly the needLoad -> bulk load -> distribute-setter cycle.
 */
@Getter
@RequiredArgsConstructor
public class EntityRef {
    final Class<?> entityClass;
    final UUID id;

    /** Loaded entity; null until EntityRefRestDTOMapper resolves the reference. */
    @Setter
    Object entity;

    public String cacheKey() {
        return entityClass.getSimpleName() + ":" + id;
    }

    @Override
    public String toString() {
        return cacheKey();
    }
}
