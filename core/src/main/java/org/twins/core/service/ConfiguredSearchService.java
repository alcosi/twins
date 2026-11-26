package org.twins.core.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.Ternary;
import org.cambium.common.util.TernaryUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.dao.annotation.ConfiguredSearch;
import org.twins.core.dao.annotation.ConfiguredSearchPredicate;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.ConfiguredSearchHolder;
import org.twins.core.domain.search.EntitySearch;
import org.twins.core.domain.search.SearchFilter;
import org.twins.core.enums.SortDirection;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.cambium.common.util.SetUtils.narrowSet;

/**
 * Base class for {@code <X>SearchConfigService} — owns the configured-search execution flow
 * (see docs/configured_search_architecture.md), so {@code <X>SearchService} stays free-search only.
 *
 * @param <S>  configured search header entity (the {@code <entity>_search} table)
 * @param <P>  predicate entity (the {@code <entity>_search_predicate} table)
 * @param <R>  domain search object
 * @param <E>  result entity
 * @param <SF> entity sort field type (a sort enum for most entities, twinClassFieldId for twin) —
 *             the SF of the free-search service; types the client sorts map keys
 */
public abstract class ConfiguredSearchService<S extends ConfiguredSearch, P extends ConfiguredSearchPredicate, R extends EntitySearch<E> & ConfiguredSearchHolder<S>, E, SF>
        extends EntitySecureFindServiceImpl<S> {

    @Autowired
    protected AuthService authService;

    @Autowired
    protected FeaturerService featurerService;

    public PaginationResult<E> findConfigured(UUID searchId, Map<String, String> namedParamsMap, R narrowSearch, SimplePagination pagination) throws ServiceException {
        return findConfigured(searchId, namedParamsMap, narrowSearch, null, pagination);
    }

    /**
     * Same as {@link #findConfigured(UUID, Map, EntitySearch, SimplePagination)} plus client-requested
     * sorts: an ordered sortField→direction map (LinkedHashMap — iteration order defines ORDER BY
     * precedence). Sort precedence:
     * client sorts > configured sorter (unless force_sorting) > pagination query sort > entity default.
     */
    public PaginationResult<E> findConfigured(UUID searchId, Map<String, String> namedParamsMap, R narrowSearch, Map<SF, SortDirection> clientSorts, SimplePagination pagination) throws ServiceException {
        if (unlimitedSearchId().equals(searchId)) {
            return runFreeSearch(narrowSearch, clientSorts, pagination);
        }
        R mainSearch = buildConfiguredSearch(searchId, namedParamsMap, narrowSearch);
        if (mainSearch.isEmptySearch()) {
            return PaginationResult.EMPTY;
        }
        return runConfiguredSearch(mainSearch, clientSorts, pagination);
    }

    /**
     * Counts entities matched by the configured search: the same predicate pipeline as
     * {@link #findConfigured}, but a single COUNT query — sorting and pagination are not applied.
     */
    public long countConfigured(UUID searchId, Map<String, String> namedParamsMap, R narrowSearch) throws ServiceException {
        if (unlimitedSearchId().equals(searchId)) {
            return runFreeCount(narrowSearch != null ? narrowSearch : entitySearchService().newSearch());
        }
        R mainSearch = buildConfiguredSearch(searchId, namedParamsMap, narrowSearch);
        if (mainSearch.isEmptySearch()) {
            return 0;
        }
        return runConfiguredCount(mainSearch);
    }

    /** Builds the domain search from the saved header: predicates → narrow → header attached. */
    private R buildConfiguredSearch(UUID searchId, Map<String, String> namedParamsMap, R narrowSearch) throws ServiceException {
        S searchEntity = findEntitySafe(searchId);
        R mainSearch = entitySearchService().newSearch();
        for (P predicate : loadPredicates(searchEntity)) {
            SearchFinder<R> finder = featurerService.getFeaturer(predicate.getFinderFeaturerId(), finderClass());
            finder.concatSearch(predicate.getFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);
        return mainSearch;
    }

    /**
     * Executes the configured search: filter specification + configured sorter + page fetch.
     * {@code mainSearch.getConfiguredSearch()} carries the header for the sorter step.
     * Override only when the search needs a different execution shape (see UserSearchConfigService).
     */
    protected PaginationResult<E> runConfiguredSearch(R mainSearch, Map<SF, SortDirection> clientSorts, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<E> spec = entitySearchService().createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
        spec = addConfiguredSorting(mainSearch, clientSorts, pagination, spec);
        Page<E> page = searchExecutor().findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    /**
     * Executes the configured count: filter specification only (no sorter, no pageable) + COUNT query.
     * Override together with {@link #runConfiguredSearch} when the search needs a different execution shape.
     */
    protected long runConfiguredCount(R mainSearch) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<E> spec = entitySearchService().createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
        return searchExecutor().count(spec);
    }

    /**
     * Applies the configured sorter when it is forced or the client did not request a sort (neither
     * request-body {@code sorts} nor the pagination query sort). Sort precedence:
     * client sorts > configured sorter (unless force_sorting) > pagination query sort > entity default.
     */
    protected Specification<E> addConfiguredSorting(R mainSearch, Map<SF, SortDirection> clientSorts, SimplePagination pagination, Specification<E> spec) throws ServiceException {
        S searchEntity = mainSearch.getConfiguredSearch();
        boolean hasClientSorts = MapUtils.isNotEmpty(clientSorts);
        boolean hasPaginationSort = pagination != null && pagination.getSort() != null;
        if (searchEntity != null && (Boolean.TRUE.equals(searchEntity.getForceSorting()) || (!hasClientSorts && !hasPaginationSort))) {
            SearchSorter<E> sorter = featurerService.getFeaturer(searchEntity.getSorterFeaturerId(), sorterClass());
            var sortFunction = sorter.createSort(searchEntity.getSorterParams());
            if (sortFunction != null) {
                spec = sortFunction.apply(spec);
                if (pagination != null) {
                    pagination.setSort(null);
                }
                return spec;
            }
        }
        if (hasClientSorts) {
            return addClientSorting(clientSorts, pagination, spec); // clears the pagination query sort inside
        }
        // No configured sort applied and the client did not request one: fall back to the entity
        // default sort to keep offset pagination deterministic.
        if (!hasPaginationSort) {
            Specification<E> defaultSort = createDefaultSortSpecification(authService.getApiUser().getLocale());
            if (defaultSort != null) {
                spec = spec.and(defaultSort);
            }
        }
        return spec;
    }

    /**
     * Client-requested sorts, applied by the free-search service (Specification-based — never
     * Pageable sort, see docs/api_sorting_architecture.md); map iteration order defines ORDER BY
     * precedence. The pagination query sort is cleared inside when sorts are applied.
     */
    protected Specification<E> addClientSorting(Map<SF, SortDirection> clientSorts, SimplePagination pagination, Specification<E> spec) throws ServiceException {
        return entitySearchService().addClientSorting(clientSorts, pagination, spec);
    }

    /**
     * Free-search service for the entity — the source of fresh search instances, the sort field
     * type and the entity default sort fallback (when its {@code defaultSortField()} is non-null).
     */
    protected abstract EntitySearchService<R, E, SF, ?> entitySearchService();

    /**
     * Entity default sort for the configured pipeline, used when no configured sorter produced a sort
     * and the client did not request one. Delegates to the free-search service's {@code defaultSortField()}
     * (ASC); a null default sort field keeps legacy behavior — no ORDER BY.
     */
    protected Specification<E> createDefaultSortSpecification(Locale locale) throws ServiceException {
        SF defaultSortField = entitySearchService().defaultSortField();
        if (defaultSortField == null) {
            return null;
        }
        return entitySearchService().createSortSpecification(defaultSortField, SortDirection.ASC, locale);
    }

    /**
     * Intersects the client filter with the configured search (restriction only, never extension):
     * narrows all {@link SearchFilter}-annotated fields generically (scanned once per class,
     * then cached), then calls {@link #narrowSearchSpecific} for cases the scan cannot express.
     */
    protected void narrowSearch(R mainSearch, R narrowSearch) {
        if (narrowSearch == null) {
            return;
        }
        for (FieldNarrowing fieldNarrowing : NARROWING_CACHE.computeIfAbsent(mainSearch.getClass(), ConfiguredSearchService::scanNarrowableFields)) {
            fieldNarrowing.narrow(mainSearch, narrowSearch);
        }
        narrowSearchSpecific(mainSearch, narrowSearch);
    }

    /** Hook for narrowing that the generic {@link SearchFilter} scan cannot express. */
    protected void narrowSearchSpecific(R mainSearch, R narrowSearch) {
    }

    /** The system "no configured search" row id — short-circuits to free search. */
    protected abstract UUID unlimitedSearchId();

    protected abstract List<P> loadPredicates(S searchEntity);

    protected abstract Class<? extends SearchFinder<R>> finderClass();

    protected abstract Class<? extends SearchSorter<E>> sorterClass();

    protected abstract JpaSpecificationExecutor<E> searchExecutor();

    /**
     * UNLIMITED path: plain free search over the narrow filter.
     * Override only when free search has a custom execution shape (see UserSearchConfigService).
     */
    protected PaginationResult<E> runFreeSearch(R search, Map<SF, SortDirection> clientSorts, SimplePagination pagination) throws ServiceException {
        if (MapUtils.isEmpty(clientSorts)) {
            return entitySearchService().search(search, pagination);
        }
        return entitySearchService().search(search, pagination, clientSorts);
    }

    /**
     * UNLIMITED count path: plain free count over the narrow filter.
     * Override together with {@link #runFreeSearch} when free search has a custom execution shape.
     */
    protected long runFreeCount(R search) throws ServiceException {
        return entitySearchService().count(search);
    }

    // Generic narrowing over @SearchFilter fields. Narrowed types: Set, Ternary, Map<UUID,Boolean>
    // (map entries carry a Boolean flag, narrowed with Boolean.TRUE semantics). Fields of any other
    // type (ranges, hierarchy objects) are skipped — the annotation marks them as filter fields,
    // but narrowing for them is not implemented yet.

    private static final Map<Class<?>, List<FieldNarrowing>> NARROWING_CACHE = new ConcurrentHashMap<>();

    private static List<FieldNarrowing> scanNarrowableFields(Class<?> searchClass) {
        List<FieldNarrowing> result = new ArrayList<>();
        for (Class<?> clazz = searchClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()) || !field.isAnnotationPresent(SearchFilter.class))
                    continue;
                FieldNarrowing fieldNarrowing = fieldNarrowingFor(field);
                if (fieldNarrowing != null) {
                    field.setAccessible(true);
                    result.add(fieldNarrowing);
                }
            }
        }
        return List.copyOf(result);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static FieldNarrowing fieldNarrowingFor(Field field) {
        Class<?> type = field.getType();
        if (Set.class.isAssignableFrom(type))
            return (main, narrow) -> writeField(field, main, narrowSet((Set) readField(field, main), (Set) readField(field, narrow)));
        if (Ternary.class.isAssignableFrom(type))
            return (main, narrow) -> writeField(field, main, TernaryUtils.narrow((Ternary) readField(field, main), (Ternary) readField(field, narrow)));
        if (Map.class.isAssignableFrom(type))
            return (main, narrow) -> writeField(field, main, MapUtils.narrowMapOfBooleans((Map<UUID, Boolean>) readField(field, main), (Map<UUID, Boolean>) readField(field, narrow), Boolean.TRUE));
        return null;
    }

    private static Object readField(Field field, Object search) {
        try {
            return field.get(search);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeField(Field field, Object search, Object value) {
        try {
            field.set(search, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private interface FieldNarrowing {
        void narrow(Object mainSearch, Object narrowSearch);
    }
}
