package org.twins.core.featurer.twin.sorter;

import jakarta.persistence.criteria.*;
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
import org.twins.core.service.SystemEntityService;

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
        return true;
    }

    private String getTwinEntityField(UUID fieldId) {
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_NAME.equals(fieldId))
            return TwinEntity.Fields.name;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION.equals(fieldId))
            return TwinEntity.Fields.description;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID.equals(fieldId))
            return TwinEntity.Fields.externalId;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_OWNER_USER.equals(fieldId))
            return TwinEntity.Fields.ownerUserId;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER.equals(fieldId))
            return TwinEntity.Fields.assignerUserId;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATOR_USER.equals(fieldId))
            return TwinEntity.Fields.createdByUserId;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_HEAD.equals(fieldId))
            return TwinEntity.Fields.headTwinId;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_STATUS.equals(fieldId))
            return TwinEntity.Fields.twinStatusId;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATED_AT.equals(fieldId))
            return TwinEntity.Fields.createdAt;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_ID.equals(fieldId))
            return TwinEntity.Fields.id;
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_TWIN_CLASS_ID.equals(fieldId))
            return TwinEntity.Fields.twinClassId;
        return null;
    }
}
