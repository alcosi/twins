package org.twins.core.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.domain.search.ConfiguredSearch;
import org.twins.core.domain.search.ConfiguredSearchHolder;
import org.twins.core.domain.search.ConfiguredSearchPredicate;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for {@code <X>SearchConfigService} — owns the configured-search execution flow
 * (see docs/configured_search_architecture.md), so {@code <X>SearchService} stays free-search only.
 *
 * @param <S> configured search header entity (the {@code <entity>_search} table)
 * @param <P> predicate entity (the {@code <entity>_search_predicate} table)
 * @param <R> domain search object
 * @param <E> result entity
 */
public abstract class ConfiguredSearchService<S extends ConfiguredSearch, P extends ConfiguredSearchPredicate, R extends ConfiguredSearchHolder<S>, E>
        extends EntitySecureFindServiceImpl<S> {

    @Autowired
    protected AuthService authService;
    @Autowired
    protected FeaturerService featurerService;

    public PaginationResult<E> findConfigured(UUID searchId, Map<String, String> namedParamsMap, R narrowSearch, SimplePagination pagination) throws ServiceException {
        if (unlimitedSearchId().equals(searchId)) {
            return runFreeSearch(narrowSearch, pagination);
        }
        S searchEntity = findEntitySafe(searchId);
        R mainSearch = emptySearch();
        for (P predicate : loadPredicates(searchEntity)) {
            SearchFinder<R> finder = featurerService.getFeaturer(predicate.getFinderFeaturerId(), finderClass());
            finder.concatSearch(predicate.getFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);
        return runConfiguredSearch(mainSearch, pagination);
    }

    /**
     * Applies the configured sorter when it is forced or the client did not request a sort.
     * For services whose configured path falls back to the free pipeline when no sorter applies
     * (twin_class, twin_class_field), keep the sorter block inside {@link #runConfiguredSearch} instead.
     */
    protected Specification<E> addConfiguredSorting(R mainSearch, SimplePagination pagination, Specification<E> spec, Class<? extends SearchSorter<E>> sorterClass) throws ServiceException {
        S searchEntity = mainSearch.getConfiguredSearch();
        if (searchEntity != null && (Boolean.TRUE.equals(searchEntity.getForceSorting()) || pagination == null || pagination.getSort() == null)) {
            SearchSorter<E> sorter = featurerService.getFeaturer(searchEntity.getSorterFeaturerId(), sorterClass);
            var sortFunction = sorter.createSort(searchEntity.getSorterParams());
            if (sortFunction != null) {
                spec = sortFunction.apply(spec);
                if (pagination != null) {
                    pagination.setSort(null);
                }
            }
        }
        return spec;
    }

    /** The system "no configured search" row id — short-circuits to free search. */
    protected abstract UUID unlimitedSearchId();

    protected abstract R emptySearch();

    protected abstract List<P> loadPredicates(S searchEntity);

    protected abstract Class<? extends SearchFinder<R>> finderClass();

    /** Intersects the client filter with the configured search (restriction only). */
    protected abstract void narrowSearch(R mainSearch, R narrowSearch);

    /** UNLIMITED path: plain free search over the narrow filter. */
    protected abstract PaginationResult<E> runFreeSearch(R search, SimplePagination pagination) throws ServiceException;

    /** Executes the configured search; {@code mainSearch.getConfiguredSearch()} carries the header for the sorter step. */
    protected abstract PaginationResult<E> runConfiguredSearch(R mainSearch, SimplePagination pagination) throws ServiceException;
}
