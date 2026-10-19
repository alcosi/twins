package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public interface SortField<T> {

    String[] fieldPath();

    default Specification<T> toSortSpecification(boolean ascending) {
        return (root, query, cb) -> {
            if (query.getResultType().equals(Long.class))
                return cb.conjunction();

            Path<?> sortPath = AbstractSpecification.getFieldPath(root, JoinType.LEFT, fieldPath());

            List<Order> orders = new ArrayList<>(query.getOrderList());
            orders.add(ascending ? cb.asc(sortPath) : cb.desc(sortPath));

            query.orderBy(orders);
            return cb.conjunction();
        };
    }
}
