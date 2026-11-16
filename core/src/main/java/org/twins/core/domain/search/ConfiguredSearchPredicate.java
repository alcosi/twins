package org.twins.core.domain.search;

import java.util.HashMap;

/**
 * Configured search predicate contract (a row of the <entity>_search_predicate table):
 * one finder featurer invocation that accumulates criteria into a domain search object.
 * See docs/configured_search_architecture.md.
 */
public interface ConfiguredSearchPredicate {
    Integer getFinderFeaturerId();

    HashMap<String, String> getFinderParams();
}
