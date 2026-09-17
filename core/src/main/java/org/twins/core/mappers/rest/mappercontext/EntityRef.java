package org.twins.core.mappers.rest.mappercontext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Not-yet-loaded reference to an entity used as a featurer param value.
 * Postponed into {@link MapperContext} during the mapping phase and bulk-loaded later
 * (grouped by {@link #entityClass}) by EntityRefRestDTOMapper on the related objects drain phase.
 */
@Getter
@RequiredArgsConstructor
public class EntityRef {
    final Class<?> entityClass;
    final UUID id;

    public String cacheKey() {
        return entityClass.getSimpleName() + ":" + id;
    }

    @Override
    public String toString() {
        return cacheKey();
    }
}
