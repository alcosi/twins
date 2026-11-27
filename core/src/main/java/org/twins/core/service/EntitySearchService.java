package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.dao.specifications.CountQueryExecutor;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.domain.CountResult;
import org.twins.core.domain.search.EntitySearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.service.auth.AuthService;

import java.util.*;

@Slf4j
public abstract class EntitySearchService<S extends EntitySearch<E>, E, SF, GF> {
    @Autowired
    protected AuthService authService;
    @Autowired
    protected CountQueryExecutor countQueryExecutor;

    public abstract JpaSpecificationExecutor<E> jpaSpecificationExecutor();

    public abstract S newSearch();

    protected abstract Class<E> entityClass();

    public abstract Specification<E> createFilterSpecification(S search, UUID domainId, Locale locale) throws ServiceException;

    public abstract Specification<E> createSortSpecification(SF sortField, SortDirection sortDirection, Locale locale) throws ServiceException;

    public abstract String convertToEntityField(GF groupFields) throws ServiceException;

    public PaginationResult<E> search(S search, SimplePagination pagination) throws ServiceException {
        return search(search, pagination, null, null);
    }

    public PaginationResult<E> search(S search, SimplePagination pagination, SF sortField, SortDirection sortDirection) throws ServiceException {
        if (sortField == null)
            sortField = defaultSortField();
        Map<SF, SortDirection> sortFields;
        if (sortField != null) {
            sortFields = Map.of(sortField, sortDirection != null ? sortDirection : SortDirection.ASC);
        } else
            sortFields = Collections.emptyMap();
        return search(search, pagination, sortFields);
    }

    /**
     * Default sort field applied when the caller passes no sortField.
     * Every search service must explicitly declare its default;
     * returning null keeps legacy behavior: no ORDER BY.
     */
    protected abstract SF defaultSortField();

    public PaginationResult<E> search(S search, SimplePagination pagination, Map<SF, SortDirection> sortFields) throws ServiceException {
        if (search == null)
            search = newSearch();
        Specification<E> spec = addClientSorting(sortFields, pagination, createFilterSpecification(search));
        Page<E> page = jpaSpecificationExecutor().findAll(spec, PaginationUtils.pageableOffset(pagination.setSort(null)));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    /**
     * No-pagination search: the whole filtered list, ordered by the entity default sort
     * ({@link #defaultSortField()}, ASC); a null default sort field keeps no ORDER BY.
     */
    public List<E> search(S search) throws ServiceException {
        if (search == null)
            search = newSearch();
        Specification<E> spec = createFilterSpecification(search);
        SF defaultSortField = defaultSortField();
        if (defaultSortField != null) {
            Specification<E> sortSpec = createSortSpecification(defaultSortField, SortDirection.ASC, authService.getApiUser().getLocale());
            if (sortSpec != null)
                spec = Specification.allOf(spec, sortSpec);
        }
        return jpaSpecificationExecutor().findAll(spec);
    }

    /**
     * No-pagination search over a projection: entities are converted to the projection type by
     * Spring Data inside the same query (see spring-projects/spring-data-jpa#430). No ORDER BY.
     */
    public <R> List<R> search(S search, Class<R> projection) throws ServiceException {
        if (search == null)
            search = newSearch();
        return jpaSpecificationExecutor().findBy(createFilterSpecification(search), q -> q.as(projection).all());
    }

    /**
     * Client-requested sorts via {@link #createSortSpecification} (Specification-based — never
     * Pageable sort, see docs/api_sorting_architecture.md); map iteration order defines ORDER BY
     * precedence. The pagination query sort is cleared when sorts are applied — the two sort
     * sources must not conflict (Spring Data JPA #2253). Empty sorts are a no-op.
     */
    protected Specification<E> addClientSorting(Map<SF, SortDirection> clientSorts, SimplePagination pagination, Specification<E> specification) throws ServiceException {
        if (MapUtils.isEmpty(clientSorts))
            return specification;
        Locale locale = authService.getApiUser().getLocale();
        for (var entry : clientSorts.entrySet()) {
            var sortSpec = createSortSpecification(entry.getKey(), entry.getValue(), locale);
            if (sortSpec != null)
                specification = Specification.allOf(specification, sortSpec);
        }
        if (pagination != null)
            pagination.setSort(null);
        return specification;
    }

    public Map<GF, Long> countByGroupFields(S search, GF groupField) throws ServiceException {
        if (search == null)
            search = newSearch();
        Specification<E> filterSpec = createFilterSpecification(search);
        var entityGroupFields = List.of(convertToEntityField(groupField));
        List<Object[]> rows = countQueryExecutor.executeGroupedCount(entityClass(), filterSpec, entityGroupFields);
        Map<GF, Long> resultMap = new HashMap<>();
        for (Object[] result : rows) resultMap.put((GF) result[0], (Long) result[1]);
        return resultMap;
    }

    public List<CountResult<E, GF>> countByGroupFields(S search, Set<GF> groupFields) throws ServiceException {
        if (search == null)
            search = newSearch();
        Specification<E> filterSpec = createFilterSpecification(search);

        if (groupFields == null || groupFields.isEmpty()) {
            long total = jpaSpecificationExecutor().count(filterSpec);
            return List.of(new CountResult<E, GF>().setCount(total));
        }

        List<GroupExpressionProvider<E>> providers = resolveGroupProviders(search, groupFields);
        List<Object[]> rows;
        if (providers != null) {
            rows = countQueryExecutor.executeGroupedCountByProviders(entityClass(), filterSpec, providers);
        } else {
            List<String> entityGroupFields = new ArrayList<>(groupFields.size());
            for (var field : groupFields) {
                entityGroupFields.add(convertToEntityField(field));
            }
            rows = countQueryExecutor.executeGroupedCount(entityClass(), filterSpec, entityGroupFields);
        }
        return mapCountResults(rows, groupFields);
    }

    /**
     * Opt-in hook for services whose group dimensions need JOINs (dynamic fields, links).
     * Default {@code null} keeps the legacy {@link #convertToEntityField(GF)} String path
     * (direct root fields only). Override to return one provider per groupField, in the SAME
     * iteration order as {@code groupFields} (the row columns are read back in that order).
     */
    protected List<GroupExpressionProvider<E>> resolveGroupProviders(S search, Set<GF> groupFields) throws ServiceException {
        return null;
    }

    private List<CountResult<E, GF>> mapCountResults(
            List<Object[]> rows,
            Set<GF> groupFields) {
        return rows.stream().map(row -> {
            E entity = newEntity();
            int i = 0;
            for (GF field : groupFields) {
                mapGroupedField(entity, field, row[i]);
                i++;
            }
            return new CountResult<E, GF>()
                    .setEntity(entity)
                    .setCount((Long) row[i])
                    .setGroupFields(groupFields);
        }).toList();
    }

    public abstract void mapGroupedField(E entity, GF field, Object o);

    protected abstract E newEntity();

    public PaginationResult<CountResult<E, GF>> countByGroupFields(S search, Set<GF> groupFields, SimplePagination pagination) throws ServiceException {
        if (search == null)
            search = newSearch();
        Specification<E> filterSpec = createFilterSpecification(search);

        if (groupFields == null || groupFields.isEmpty()) {
            long total = jpaSpecificationExecutor().count(filterSpec);
            PaginationResult<CountResult<E, GF>> result = new PaginationResult<>();
            result
                    .setList(List.of(new CountResult<E, GF>().setCount(total)))
                    .setTotal(1)
                    .setOffset(pagination.getOffset())
                    .setLimit(pagination.getLimit());
            return result;
        }

        List<GroupExpressionProvider<E>> providers = resolveGroupProviders(search, groupFields);
        Page<Object[]> page;
        if (providers != null) {
            page = countQueryExecutor.executeGroupedCountPaginatedByProviders(entityClass(), filterSpec, providers, pagination);
        } else {
            List<String> entityGroupFields = new ArrayList<>(groupFields.size());
            for (var field : groupFields) {
                entityGroupFields.add(convertToEntityField(field));
            }
            page = countQueryExecutor.executeGroupedCountPaginated(entityClass(), filterSpec, entityGroupFields, pagination);
        }
        List<CountResult<E, GF>> results = mapCountResults(page.getContent(), groupFields);
        PaginationResult<CountResult<E, GF>> pr = new PaginationResult<>();
        pr.setList(results);
        pr.setTotal(page.getTotalElements());
        pr.setOffset(pagination.getOffset());
        pr.setLimit(pagination.getLimit());
        return pr;
    }

    protected Specification<E> createFilterSpecification(S search) throws ServiceException {
        var apiUser = authService.getApiUser();
        return createFilterSpecification(search, apiUser.getDomainId(), apiUser.getLocale());
    }

    public Long count(Specification<E> spec) throws ServiceException {
        return jpaSpecificationExecutor().count(spec);
    }

    public Long count(S search) throws ServiceException {
        return count(createFilterSpecification(search));
    }

    public Long count(List<S> searchList) throws ServiceException {
        Specification<E> spec = (root, query, builder) -> builder.disjunction();
        for (var search : searchList)
            spec = spec.or(createFilterSpecification(search));
        return count(spec);
    }

    public Map<String, Long> countBatch(Map<String, S> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, S> entry : searchMap.entrySet())
            result.put(entry.getKey(), count(entry.getValue()));
        return result;
    }

    public boolean exists(Specification<E> spec) throws ServiceException {
        return jpaSpecificationExecutor().exists(spec);
    }

    public boolean exists(S search) throws ServiceException {
        return exists(createFilterSpecification(search));
    }

}
