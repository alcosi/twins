package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.domain.CountResult;
import org.twins.core.domain.search.EntitySearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
public abstract class EntitySearchService<S extends EntitySearch<E>, E, SF, GF> {
    @Autowired
    protected AuthService authService;

    public abstract JpaSpecificationExecutor<E> jpaSpecificationExecutor();

    public abstract S emptySearch();

    public abstract Specification<E> createFilterSpecification(S search, UUID domainId);

    public abstract Specification<E> createSortSpecification(SF sortField, SortDirection sortDirection);

    public abstract Specification<E> createCountSpecification(Set<GF> groupFields);

    public PaginationResult<E> search(S search, SimplePagination pagination) throws ServiceException {
        return search(search, pagination, null, null);
    }

    public PaginationResult<E> search(S search, SimplePagination pagination, SF sortField, SortDirection sortDirection) throws ServiceException {
        if (search == null)
            search = emptySearch();
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<E> spec;
        if (sortField != null) {
            spec = Specification.allOf(
                    createFilterSpecification(search, domainId),
                    createSortSpecification(sortField, sortDirection));
        } else {
            spec = createFilterSpecification(search, domainId);
        }
        Page<E> page = jpaSpecificationExecutor().findAll(spec, PaginationUtils.pageableOffset(pagination.setSort(null)));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    public List<CountResult<E>> countByGroupFields(S search, Set<GF> groupFields) throws ServiceException {
        if (search == null)
            search = emptySearch();
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<E> filterSpec = createFilterSpecification(search, domainId);

        if (groupFields == null || groupFields.isEmpty()) {
            long total = jpaSpecificationExecutor().count(filterSpec);
            return List.of(new CountResult<E>().setCount(total));
        }

        Specification<E> countSpec = createCountSpecification(groupFields);
        List<Object[]> rows = (List<Object[]>) jpaSpecificationExecutor()
                .findAll(Specification.allOf(filterSpec, countSpec));
        return mapCountResults(rows, groupFields);
    }

    private List<CountResult<E>> mapCountResults(
            List<Object[]> rows,
            Set<GF> groupFields) {
        return rows.stream().map(row -> {
            E entity = newEntity();
            int i = 0;
            for (GF field : groupFields) {
                mapGroupedField(entity, field, row[i]);
                i++;
            }
            return new CountResult<E>()
                    .setEntity(entity)
                    .setCount((Long) row[i]);
        }).toList();
    }

    public abstract void mapGroupedField(E entity, GF field, Object o);

    protected abstract E newEntity();

//    public abstract List<CountResult<E>> countByGroupFields(S search, Set<GF> groupFields) throws ServiceException;

}
