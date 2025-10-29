package org.twins.core.featurer.twin.sorter;

import jakarta.persistence.criteria.*;
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
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4105,
        name = "Sort by number field",
        description = "Sort twins by numeric value stored as string with NULLS LAST")
public class TwinSorterNumberField extends TwinSorter {
    @Override
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        return baseSpec -> (root, query, cb) -> {
            Predicate basePredicate = baseSpec == null ? null : baseSpec.toPredicate(root, query, cb);
            if (!query.getResultType().equals(Long.class)) {
                List<Order> orders = new ArrayList<>();
                // Get or create JOIN
                Join<TwinEntity, TwinFieldSimpleEntity> tfJoin = getOrCreateJoin(root, cb, fieldId, TwinEntity.Fields.fieldsSimple);
                // Convert string value to Double for numeric sorting
                Expression<String> stringValue = tfJoin.get(TwinFieldSimpleEntity.Fields.value);
                Expression<Double> numericValue = cb.function("text2double", Double.class, stringValue);
                // Ensure NULL values are placed at the end
                addNullsPositionOrder(orders, cb, tfJoin, TwinFieldSimpleEntity.Fields.value, properties);
                // Sort by numeric value
                addValueAndCombineOrders(orders, cb, query, numericValue, direction);

            }
            return basePredicate;
        };
    }

    @Override
    public boolean checkCompatibleSorter(FieldTyper fieldTyper) {
        return fieldTyper.getStorageType().equals(TwinFieldStorageSimple.class);
    }
}
