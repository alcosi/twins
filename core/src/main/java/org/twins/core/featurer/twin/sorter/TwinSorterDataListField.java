package org.twins.core.featurer.twin.sorter;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.hibernate.query.SortDirection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSelect;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4106,
        name = "Sort by datalist field",
        description = "Sort twins by datalist field(not multiple) value using order field from data_list_option with configurable NULL position")
public class TwinSorterDataListField extends TwinSorter {
    @Override
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, TwinClassFieldEntity twinClassFieldEntity, SortDirection direction) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        return baseSpec -> (root, query, cb) -> {
            Predicate basePredicate = baseSpec == null ? null : baseSpec.toPredicate(root, query, cb);
            if (!query.getResultType().equals(Long.class)) {
                List<Order> orders = new ArrayList<>();
                // Get or create JOIN to twin_field_data_list
                Join<TwinEntity, TwinFieldDataListEntity> tfJoin = getOrCreateJoin(root, cb, fieldId, TwinEntity.Fields.fieldsList);
                // Join to data_list_option to get order field
                Join<TwinFieldDataListEntity, DataListOptionEntity> dloJoin = tfJoin.join(TwinFieldDataListEntity.Fields.dataListOption, JoinType.LEFT);
                // Get order field for sorting
                Expression<Short> orderValue = dloJoin.get(DataListOptionEntity.Fields.order);
                // Handle NULL position
                addNullsPositionOrder(orders, cb, tfJoin, TwinFieldDataListEntity.Fields.dataListOptionId, properties);
                // Sort by order value and combine with existing orders
                addValueAndCombineOrders(orders, cb, query, orderValue, direction);
            }
            return basePredicate;
        };
    }

    @Override
    public boolean checkCompatibleSorter(FieldTyper fieldTyper) {
        return fieldTyper instanceof FieldTyperSelect && fieldTyper.getStorageType().equals(TwinFieldStorageDatalist.class);
    }
}
