package org.twins.core.featurer.twin.counter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5404, name = "Group by date field", description = "Count twins grouped by date/time field value")
public class TwinCounterDateField extends TwinCounter {
    @Override
    public GroupExpressionProvider<TwinEntity> createGroup(Properties properties, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        return (root, query, cb) -> {
            Join<TwinEntity, TwinFieldTimestampEntity> tfJoin = TwinSpecification.getOrCreateFieldJoin(root, cb, fieldId, TwinEntity.Fields.fieldsTimestamp, JoinType.LEFT);
            return tfJoin.get(TwinFieldTimestampEntity.Fields.value);
        };
    }

    @Override
    public boolean checkCompatibleCounter(FieldTyper fieldTyper) {
        return fieldTyper.getStorageType().equals(TwinFieldStorageTimestamp.class);
    }
}
