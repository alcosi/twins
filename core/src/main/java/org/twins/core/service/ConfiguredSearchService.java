package org.twins.core.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.ConfiguredSearch;
import org.twins.core.domain.search.ConfiguredSearchHolder;
import org.twins.core.domain.search.ConfiguredSearchPredicate;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Locale;
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
        R mainSearch = newSearch();
        for (P predicate : loadPredicates(searchEntity)) {
            SearchFinder<R> finder = featurerService.getFeaturer(predicate.getFinderFeaturerId(), finderClass());
            finder.concatSearch(predicate.getFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);
        if (mainSearch.isEmptySearch()) {
            return PaginationResult.EMPTY;
        }
        return runConfiguredSearch(mainSearch, pagination);
    }

    /**
     * Executes the configured search: filter specification + configured sorter + page fetch.
     * {@code mainSearch.getConfiguredSearch()} carries the header for the sorter step.
     * Override only when the search needs a different execution shape (see UserSearchConfigService).
     */
    protected PaginationResult<E> runConfiguredSearch(R mainSearch, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<E> spec = createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
        spec = addConfiguredSorting(mainSearch, pagination, spec);
        Page<E> page = searchExecutor().findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    /** Applies the configured sorter when it is forced or the client did not request a sort. */
    protected Specification<E> addConfiguredSorting(R mainSearch, SimplePagination pagination, Specification<E> spec) throws ServiceException {
        S searchEntity = mainSearch.getConfiguredSearch();
        if (searchEntity != null && (Boolean.TRUE.equals(searchEntity.getForceSorting()) || pagination == null || pagination.getSort() == null)) {
            SearchSorter<E> sorter = featurerService.getFeaturer(searchEntity.getSorterFeaturerId(), sorterClass());
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

    /** A fresh search instance to accumulate predicates into (may carry non-default flags). */
    protected abstract R newSearch();

    protected abstract List<P> loadPredicates(S searchEntity);

    protected abstract Class<? extends SearchFinder<R>> finderClass();

    protected abstract Class<? extends SearchSorter<E>> sorterClass();

    /** Intersects the client filter with the configured search (restriction only). */
    protected abstract void narrowSearch(R mainSearch, R narrowSearch);

    protected abstract Specification<E> createFilterSpecification(R search, UUID domainId, Locale locale) throws ServiceException;

    protected abstract JpaSpecificationExecutor<E> searchExecutor();

    /** UNLIMITED path: plain free search over the narrow filter. */
    protected abstract PaginationResult<E> runFreeSearch(R search, SimplePagination pagination) throws ServiceException;
}
