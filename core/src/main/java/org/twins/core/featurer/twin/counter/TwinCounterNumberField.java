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
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5802, name = "Group by number field", description = "Count twins grouped by decimal field value")
public class TwinCounterNumberField extends TwinCounter {
    @Override
    public GroupExpressionProvider<TwinEntity> createGroup(Properties properties, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        return (root, query, cb) -> {
            Join<TwinEntity, TwinFieldDecimalEntity> tfJoin = TwinSpecification.getOrCreateFieldJoin(root, cb, fieldId, TwinEntity.Fields.fieldsDecimalSpecOnly, JoinType.LEFT);
            return tfJoin.get(TwinFieldDecimalEntity.Fields.value);
        };
    }

    @Override
    public boolean checkCompatibleCounter(FieldTyper fieldTyper) {
        return fieldTyper.getStorageType().equals(TwinFieldStorageDecimal.class);
    }
}
