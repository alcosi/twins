package org.twins.core.featurer.twin.sorter;

import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.hibernate.query.SortDirection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4107, name = "Twin Sorter System Field", description = "Sorter for system fields (Twin Entity fields)")
public class TwinSorterTwinField extends TwinSorter {

    @Override
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        String fieldName = getTwinEntityField(fieldId);
        return baseSpec -> (root, query, cb) -> {
            Predicate basePredicate = baseSpec == null ? null : baseSpec.toPredicate(root, query, cb);
            if (fieldName != null && !query.getResultType().equals(Long.class)) {
                List<Order> orders = new ArrayList<>();
                Path<?> fieldPath = root.get(fieldName);

                addNullsPositionOrder(orders, cb, root, fieldName, properties);

                addValueAndCombineOrders(orders, cb, query, fieldPath, direction);
            }
            return basePredicate;
        };
    }

    @Override
    public boolean checkCompatibleSorter(FieldTyper fieldTyper) throws ServiceException {
        return fieldTyper.getStorageType().equals(TwinFieldStorageTwin.class);
    }

    private String getTwinEntityField(UUID fieldId) {
        var basicField = TwinEntity.BasicField.convertOrNull(fieldId);
        return basicField != null ? basicField.getName() : null;
    }
}
