package org.twins.core.domain.usage;

import lombok.Getter;
import org.twins.core.mappers.rest.mappercontext.EntityRef;

import java.util.UUID;

/**
 * A single usage of an entity: where it is referenced from. Extends {@link EntityRef} so the
 * referencing entity is a lazy reference resolvable in bulk via EntityServiceRegistry: the
 * inherited {@code id} + {@code entityClass} (taken from the usage type) identify the referencing
 * entity (pipeline, branch, transition, twinflow_factory, ...), the loaded instance lands in the
 * inherited {@code entity} field and is postponed into relatedObjects by UsageRestDTOMapper.
 */
@Getter
public class Usage extends EntityRef {
    private final UsageType usageType;

    public Usage(UsageType usageType, UUID id) {
        super(usageType.getEntityClass(), id);
        this.usageType = usageType;
    }

    @Override
    public String toString() {
        return "usage[" + usageType + " " + cacheKey() + "]";
    }
}
