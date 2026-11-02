package org.twins.core.service.twin;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSelect;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;

import java.util.Properties;
import java.util.UUID;

/**
 * GROUP BY for a dynamic twin class field, chosen from the field typer storage.
 * No per-field counter configuration: the storage already says which value table to join.
 */
@Component
@RequiredArgsConstructor
public class TwinFieldCountGroup {
    private final FeaturerService featurerService;

    public GroupExpressionProvider<TwinEntity> create(TwinClassFieldEntity field) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        Class<?> storage = fieldTyper.getStorageType();
        UUID fieldId = field.getId();
        if (storage.equals(TwinFieldStorageSimple.class)) {
            return value(fieldId, TwinEntity.Fields.fieldsSimpleSpecOnly, TwinFieldSimpleEntity.Fields.value);
        }
        if (storage.equals(TwinFieldStorageDecimal.class)) {
            return value(fieldId, TwinEntity.Fields.fieldsDecimalSpecOnly, TwinFieldDecimalEntity.Fields.value);
        }
        if (storage.equals(TwinFieldStorageBoolean.class)) {
            return value(fieldId, TwinEntity.Fields.fieldsBooleanSpecOnly, TwinFieldBooleanEntity.Fields.value);
        }
        if (storage.equals(TwinFieldStorageTimestamp.class)) {
            return value(fieldId, TwinEntity.Fields.fieldsTimestampSpecOnly, TwinFieldTimestampEntity.Fields.value);
        }
        if (storage.equals(TwinFieldStorageDatalist.class)) {
            return datalist(fieldId, isMultipleSelect(fieldTyper, field));
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "Field " + fieldId + " is not countable; " + field.logShort());
    }

    /**
     * A multiple select stores one row per chosen option, so the same twin matches the join
     * more than once. Count that twin once inside each option group.
     */
    private boolean isMultipleSelect(FieldTyper fieldTyper, TwinClassFieldEntity field) throws ServiceException {
        if (!(fieldTyper instanceof FieldTyperSelect)) {
            return false;
        }
        Properties properties = featurerService.extractProperties(fieldTyper, field.getFieldTyperParams());
        return FieldTyperSelect.multiple.extract(properties);
    }

    private static GroupExpressionProvider<TwinEntity> value(UUID fieldId, String collection, String attribute) {
        return (root, query, cb) -> join(root, cb, fieldId, collection).get(attribute);
    }

    private static GroupExpressionProvider<TwinEntity> datalist(UUID fieldId, boolean multiple) {
        return new GroupExpressionProvider<>() {
            @Override
            public Expression<?> apply(Root<TwinEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return join(root, cb, fieldId, TwinEntity.Fields.fieldsListSpecOnly)
                        .get(TwinFieldDataListEntity.Fields.dataListOptionId);
            }

            @Override
            public boolean isDistinctGroup() {
                return multiple;
            }
        };
    }

    private static Join<TwinEntity, ?> join(Root<TwinEntity> root, CriteriaBuilder cb, UUID fieldId, String collection) {
        return TwinSpecification.getOrCreateFieldJoin(root, cb, fieldId, collection, JoinType.LEFT);
    }
}
