package org.twins.core.dao.specifications;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CountQueryExecutor {
    @Autowired
    private EntityManager entityManager;

    public <E> List<Object[]> executeGroupedCount(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<String> groupFieldNames) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(entityClass);

        List<Expression<?>> groupBy = buildGroupBy(root, groupFieldNames);
        List<Selection<?>> selections = buildSelections(root, groupBy);

        query.multiselect(selections);
        applyFilter(query, root, filterSpec, cb);
        query.groupBy(groupBy);

        return entityManager.createQuery(query).getResultList();
    }

    public <E> Page<Object[]> executeGroupedCountPaginated(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<String> groupFieldNames,
            SimplePagination pagination) throws ServiceException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Count query - COUNT(DISTINCT) for single field, separate count for multiple
        long total = countTotalGroups(entityClass, filterSpec, groupFieldNames, cb);

        if (total == 0) {
            Pageable pageable = PaginationUtils.pageableOffset(pagination);
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Data query with pagination
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(entityClass);

        List<Expression<?>> groupBy = buildGroupBy(root, groupFieldNames);
        List<Selection<?>> selections = buildSelections(root, groupBy);

        query.multiselect(selections);
        applyFilter(query, root, filterSpec, cb);
        query.groupBy(groupBy);

        Pageable pageable = PaginationUtils.pageableOffset(pagination);
        List<Object[]> content = entityManager.createQuery(query)
                .setFirstResult(pagination.getOffset())
                .setMaxResults(pagination.getLimit())
                .getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Grouped count driven by {@link GroupExpressionProvider}s, so dimensions backed by JOINs
     * (dynamic twin fields, twin links) are supported — not only direct root fields.
     */
    public <E> List<Object[]> executeGroupedCountByProviders(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<GroupExpressionProvider<E>> groupProviders) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(entityClass);

        applyFilter(query, root, filterSpec, cb);
        List<Expression<?>> groupBy = buildGroupBy(root, query, groupProviders, cb);

        query.multiselect(buildSelections(root, groupBy, groupProviders, cb));
        query.groupBy(groupBy);

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Paginated grouped count driven by {@link GroupExpressionProvider}s.
     */
    public <E> Page<Object[]> executeGroupedCountPaginatedByProviders(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<GroupExpressionProvider<E>> groupProviders,
            SimplePagination pagination) throws ServiceException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countTotalGroupsProviders(entityClass, filterSpec, groupProviders, cb);
        if (total == 0) {
            Pageable pageable = PaginationUtils.pageableOffset(pagination);
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(entityClass);

        applyFilter(query, root, filterSpec, cb);
        List<Expression<?>> groupBy = buildGroupBy(root, query, groupProviders, cb);

        query.multiselect(buildSelections(root, groupBy, groupProviders, cb));
        query.groupBy(groupBy);

        Pageable pageable = PaginationUtils.pageableOffset(pagination);
        List<Object[]> content = entityManager.createQuery(query)
                .setFirstResult(pagination.getOffset())
                .setMaxResults(pagination.getLimit())
                .getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    private <E> long countTotalGroupsProviders(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<GroupExpressionProvider<E>> groupProviders,
            CriteriaBuilder cb) {
        // Count distinct groups by grouping on the provider expressions and counting the groups.
        // Same "load groups and count them" approach as countDistinctMultiple (line ~140 todo).
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(entityClass);
        applyFilter(query, root, filterSpec, cb);
        List<Expression<?>> groupBy = buildGroupBy(root, query, groupProviders, cb);
        List<Selection<?>> selections = new ArrayList<>(groupBy);
        query.multiselect(selections);
        query.groupBy(groupBy);
        return entityManager.createQuery(query).getResultList().size();
    }

    private <E> List<Expression<?>> buildGroupBy(
            Root<E> root,
            CriteriaQuery<?> query,
            List<GroupExpressionProvider<E>> groupProviders,
            CriteriaBuilder cb) {
        List<Expression<?>> groupBy = new ArrayList<>(groupProviders.size());
        for (GroupExpressionProvider<E> provider : groupProviders) {
            // apply() may register JOINs on root as a side effect
            groupBy.add(provider.apply(root, query, cb));
        }
        return groupBy;
    }

    private <E> List<Selection<?>> buildSelections(
            Root<E> root,
            List<Expression<?>> groupBy,
            List<GroupExpressionProvider<E>> groupProviders,
            CriteriaBuilder cb) {
        List<Selection<?>> selections = new ArrayList<>(groupBy.size() + 1);
        selections.addAll(groupBy);
        boolean distinct = groupProviders.stream().anyMatch(GroupExpressionProvider::isDistinctGroup);
        // Hibernate renders count(root)/countDistinct(root) over the entity identifier.
        selections.add(distinct ? cb.countDistinct(root) : cb.count(root));
        return selections;
    }

    private <E> long countTotalGroups(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<String> groupFieldNames,
            CriteriaBuilder cb) {
        if (groupFieldNames.size() == 1) {
            return countDistinctSingle(entityClass, filterSpec, groupFieldNames.get(0), cb);
        }
        return countDistinctMultiple(entityClass, filterSpec, groupFieldNames, cb);
    }

    private <E> long countDistinctSingle(
            Class<E> entityClass,
            Specification<E> filterSpec,
            String fieldName,
            CriteriaBuilder cb) {
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<E> root = query.from(entityClass);
        applyFilter(query, root, filterSpec, cb);
        query.select(cb.countDistinct(root.get(fieldName)));
        Long result = entityManager.createQuery(query).getSingleResult();
        if (hasNullGroup(entityClass, filterSpec, fieldName, cb)) {
            result++;
        }
        return result != null ? result : 0;
    }

    private <E> boolean hasNullGroup(Class<E> entityClass, Specification<E> filterSpec, String fieldName, CriteriaBuilder cb) {
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<E> root = query.from(entityClass);
        Predicate predicate = filterSpec != null
                ? filterSpec.toPredicate(root, query, cb)
                : null;
        Predicate isNull = cb.isNull(root.get(fieldName));
        query.select(cb.literal(1));
        query.where(predicate == null ? isNull : cb.and(predicate, isNull));

        return !entityManager.createQuery(query)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    private <E> long countDistinctMultiple(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<String> groupFieldNames,
            CriteriaBuilder cb) {
        // Build GROUP BY query selecting only group fields (no count aggregate)
        // to count distinct combinations efficiently
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(entityClass);

        List<Expression<?>> groupBy = new ArrayList<>(groupFieldNames.size());
        List<Selection<?>> selections = new ArrayList<>(groupFieldNames.size());
        for (String fieldName : groupFieldNames) {
            Path<?> path = root.get(fieldName);
            groupBy.add(path);
            selections.add(path);
        }

        query.multiselect(selections);
        applyFilter(query, root, filterSpec, cb);
        query.groupBy(groupBy);
        //todo think over, this is not good idea to load records in memory
        return entityManager.createQuery(query).getResultList().size();
    }

    private <E> List<Expression<?>> buildGroupBy(Root<E> root, List<String> groupFieldNames) {
        List<Expression<?>> groupBy = new ArrayList<>(groupFieldNames.size());
        for (String fieldName : groupFieldNames) {
            groupBy.add(root.get(fieldName));
        }
        return groupBy;
    }

    private <E> List<Selection<?>> buildSelections(Root<E> root, List<Expression<?>> groupBy) {
        List<Selection<?>> selections = new ArrayList<>(groupBy.size() + 1);
        selections.addAll(groupBy);
        selections.add(entityManager.getCriteriaBuilder().count(root));
        return selections;
    }

    private <E> void applyFilter(CriteriaQuery<?> query, Root<E> root, Specification<E> filterSpec, CriteriaBuilder cb) {
        if (filterSpec != null) {
            Predicate predicate = filterSpec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
    }

}
