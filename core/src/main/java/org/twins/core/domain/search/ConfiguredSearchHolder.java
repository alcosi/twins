package org.twins.core.domain.search;

/**
 * Domain search object that carries the configured search header it was built from,
 * so the sorting/execution step can reach it later. Lombok chained accessors
 * ({@code @Data + @Accessors(chain = true)}) satisfy this interface automatically.
 */
public interface ConfiguredSearchHolder<S extends ConfiguredSearch> {
    S getConfiguredSearch();

    ConfiguredSearchHolder<S> setConfiguredSearch(S configuredSearch);
}
