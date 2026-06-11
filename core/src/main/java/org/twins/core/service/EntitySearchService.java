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

    public abstract S emptySearch();

    protected abstract Class<E> entityClass();

    public abstract Specification<E> createFilterSpecification(S search, UUID domainId, Locale locale) throws ServiceException;

    public abstract Specification<E> createSortSpecification(SF sortField, SortDirection sortDirection, Locale locale) throws ServiceException;

    public abstract String convertToEntityField(GF groupFields) throws ServiceException;

    public PaginationResult<E> search(S search, SimplePagination pagination) throws ServiceException {
        return search(search, pagination, null, null);
    }

    public PaginationResult<E> search(S search, SimplePagination pagination, SF sortField, SortDirection sortDirection) throws ServiceException {
        Map<SF, SortDirection> sortFields;
        if (sortField != null) {
            sortFields = Map.of(sortField, sortDirection != null ? sortDirection : SortDirection.ASC);
        } else
            sortFields = Collections.emptyMap();
        return search(search, pagination, sortFields);
    }

    public PaginationResult<E> search(S search, SimplePagination pagination, Map<SF, SortDirection> sortFields) throws ServiceException {
        if (search == null)
            search = emptySearch();
        Locale locale = authService.getApiUser().getLocale();
        Specification<E> spec;
        if (MapUtils.isNotEmpty(sortFields)) {
            spec = createFilterSpecification(search);
            for (var entry : sortFields.entrySet()) {
                var sortField = entry.getKey();
                var sortDirection = entry.getValue();
                var sortSpec = createSortSpecification(sortField, sortDirection, locale);
                if (sortSpec != null) {
                    spec = Specification.allOf(spec, sortSpec);
                }
            }
        } else {
            spec = createFilterSpecification(search);
        }
        Page<E> page = jpaSpecificationExecutor().findAll(spec, PaginationUtils.pageableOffset(pagination.setSort(null)));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    public final Map<GF, Long> countByGroupFields(S search, GF groupField) throws ServiceException {
        if (search == null)
            search = emptySearch();
        Specification<E> filterSpec = createFilterSpecification(search);
        var entityGroupFields = List.of(convertToEntityField(groupField));
        List<Object[]> rows = countQueryExecutor.executeGroupedCount(entityClass(), filterSpec, entityGroupFields);
        Map<GF, Long> resultMap = new HashMap<>();
        for (Object[] result : rows) resultMap.put((GF) result[0], (Long) result[1]);
        return resultMap;
    }

    public List<CountResult<E, GF>> countByGroupFields(S search, Set<GF> groupFields) throws ServiceException {
        if (search == null)
            search = emptySearch();
        Specification<E> filterSpec = createFilterSpecification(search);

        if (groupFields == null || groupFields.isEmpty()) {
            long total = jpaSpecificationExecutor().count(filterSpec);
            return List.of(new CountResult<E, GF>().setCount(total));
        }

        List<String> entityGroupFields = new ArrayList<>(groupFields.size());
        for (var field : groupFields) {
            entityGroupFields.add(convertToEntityField(field));
        }
        List<Object[]> rows = countQueryExecutor.executeGroupedCount(entityClass(), filterSpec, entityGroupFields);
        return mapCountResults(rows, groupFields);
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
            search = emptySearch();
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

        List<String> entityGroupFields = new ArrayList<>(groupFields.size());
        for (var field : groupFields) {
            entityGroupFields.add(convertToEntityField(field));
        }

        Page<Object[]> page = countQueryExecutor.executeGroupedCountPaginated(entityClass(), filterSpec, entityGroupFields, pagination);
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
