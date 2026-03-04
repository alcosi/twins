package org.twins.core.domain;

import java.util.UUID;

/**
 * Interface for entities used in EntitiesChangesCollector.
 */
public interface Identifiable {
    UUID getId();
}
