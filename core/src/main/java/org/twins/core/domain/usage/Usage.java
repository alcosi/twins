package org.twins.core.domain.usage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * A single usage of an entity: where it is referenced from.
 * {@code entity} holds the referencing entity instance (pipeline, branch, transition,
 * twinflow_factory, ...) so that mappers can postpone it into relatedObjects.
 */
@Data
@Accessors(chain = true)
public class Usage {
    private UsageType usageType;
    private UUID id;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Object entity;
}
