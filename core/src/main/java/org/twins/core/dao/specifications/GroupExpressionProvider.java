package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

/**
 * Contributes the GROUP BY expression for one count dimension, optionally adding JOINs as a side effect.
 * Mirrors how {@code TwinSorter.createSort} returns a decorator that JOINs and appends ORDER BY:
 * here the provider JOINs (if needed) and returns the {@link Expression} to GROUP BY (and SELECT).
 *
 * @param <E> root entity type
 */
@FunctionalInterface
public interface GroupExpressionProvider<E> {

    /**
     * Build the group-by expression for this dimension. May register JOINs on the {@code root}
     * as a side effect (e.g. to a field-storage table or to {@code twin_link}).
     */
    Expression<?> apply(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    /**
     * @return {@code true} when this dimension can match the same root row multiple times
     * (e.g. a twin has several matching link rows). Forces {@code COUNT(DISTINCT root)} so each
     * root row is counted once per group.
     */
    default boolean isDistinctGroup() {
        return false;
    }

    /**
     * Provider backed by a direct root field (the legacy {@code List<String>} behaviour).
     */
    static <E> GroupExpressionProvider<E> ofField(String fieldName) {
        return (root, query, cb) -> root.get(fieldName);
    }
}
