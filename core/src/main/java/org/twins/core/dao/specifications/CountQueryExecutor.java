package org.twins.core.dao.specifications;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
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

        List<Expression<?>> groupBy = new ArrayList<>(groupFieldNames.size());
        List<Selection<?>> selections = new ArrayList<>(groupFieldNames.size() + 1);

        for (String fieldName : groupFieldNames) {
            Path<?> path = root.get(fieldName);
            groupBy.add(path);
            selections.add(path);
        }

        selections.add(cb.count(root));

        query.multiselect(selections);

        if (filterSpec != null) {
            Predicate predicate = filterSpec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        query.groupBy(groupBy);

        return entityManager.createQuery(query).getResultList();
    }
}
