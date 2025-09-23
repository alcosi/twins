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
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4103,
        name = "Sort by boolean field",
        description = "Sort twins by boolean field value")
public class TwinSorterBooleanFields extends TwinSorter {

    @Override
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        return baseSpec -> (root, query, cb) -> {
            Predicate basePredicate = baseSpec == null ? null : baseSpec.toPredicate(root, query, cb);
            if (!query.getResultType().equals(Long.class)) {
                List<Order> orders = new ArrayList<>();
                Subquery<Boolean> sub = query.subquery(Boolean.class);
                Root<TwinFieldBooleanEntity> tf = sub.from(TwinFieldBooleanEntity.class);
                sub.select(tf.get(TwinFieldBooleanEntity.Fields.value));
                sub.where(cb.and(
                        cb.equal(tf.get("twinId"), root.get("id")),
                        cb.equal(tf.get("twinClassFieldId"), fieldId)
                ));
                // Ensure NULL values are always placed at the end regardless of direction
                orders.add(cb.asc(cb.selectCase().when(cb.isNull(sub), 1).otherwise(0)));
                // Then apply actual boolean ordering according to the requested direction
                if (direction == SortDirection.DESCENDING)
                    orders.add(cb.desc(sub));
                else
                    orders.add(cb.asc(sub));
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
