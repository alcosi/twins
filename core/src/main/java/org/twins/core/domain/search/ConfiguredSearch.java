package org.twins.core.domain.search;

import java.util.HashMap;
import java.util.UUID;

/**
 * Configured search header contract (a row of the <entity>_search table).
 * See docs/configured_search_architecture.md.
 */
public interface ConfiguredSearch {
    UUID getId();

    UUID getDomainId();

    Boolean getForceSorting();

    Integer getSorterFeaturerId();

    HashMap<String, String> getSorterParams();
}
