package org.twins.core.featurer.twin.sorter;

import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.hibernate.query.SortDirection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4102,
        name = "Sort by simple field",
        description = "Sort twins by simple field value (text/number/date) with basic type-aware handling")
public class TwinSorterSimpleFields extends TwinSorter {

    @Override
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        Integer typerId = twinClassFieldEntity.getFieldTyperFeaturerId();
        return baseSpec -> (root, query, cb) -> {
            Predicate basePredicate = baseSpec == null ? null : baseSpec.toPredicate(root, query, cb);
            if (!query.getResultType().equals(Long.class)) {
                List<Order> orders = new ArrayList<>();
                // Subquery to fetch field value for current twin
                Subquery<String> sub = query.subquery(String.class);
                Root<TwinFieldSimpleEntity> tf = sub.from(TwinFieldSimpleEntity.class);
                sub.select(tf.get(TwinFieldSimpleEntity.Fields.value));
                sub.where(cb.and(
                        cb.equal(tf.get("twinId"), root.get("id")),
                        cb.equal(tf.get("twinClassFieldId"), fieldId)
                ));

                boolean isNumeric = Objects.equals(typerId, FeaturerTwins.ID_1317);
                boolean isDate = Objects.equals(typerId, FeaturerTwins.ID_1302);

                // Ensure NULL values are always placed at the end regardless of direction
                orders.add(cb.asc(cb.selectCase().when(cb.isNull(sub), 1).otherwise(0)));

                if (isNumeric) {
                    // Approximate numeric ordering: first by length, then by lexicographic value
                    var lenExpr = cb.function("length", Integer.class, sub);
                    if (direction == SortDirection.DESCENDING) {
                        orders.add(cb.desc(lenExpr));
                        orders.add(cb.desc(sub));
                    } else {
                        orders.add(cb.asc(lenExpr));
                        orders.add(cb.asc(sub));
                    }
                } else if (isDate) {
                    // Assuming ISO date/time string stored => lexicographic order works
                    if (direction == SortDirection.DESCENDING) {
                        orders.add(cb.desc(sub));
                    } else {
                        orders.add(cb.asc(sub));
                    }
                } else {
                    // Default text ordering
                    if (direction == SortDirection.DESCENDING) {
                        orders.add(cb.desc(sub));
                    } else {
                        orders.add(cb.asc(sub));
                    }
                }
                if (!orders.isEmpty()) {
                    List<Order> current = new ArrayList<>(query.getOrderList());
                    current.addAll(orders);
                    query.orderBy(current);
                }
            }
            return basePredicate;
        };
    }
}
