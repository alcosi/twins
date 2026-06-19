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
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSelect;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5406, name = "Group by datalist field", description = "Count twins grouped by selected datalist option id")
public class TwinCounterDataListField extends TwinCounter {
    @Override
    public GroupExpressionProvider<TwinEntity> createGroup(Properties properties, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        UUID fieldId = twinClassFieldEntity.getId();
        return (root, query, cb) -> {
            Join<TwinEntity, TwinFieldDataListEntity> tfJoin = TwinSpecification.getOrCreateFieldJoin(root, cb, fieldId, TwinEntity.Fields.fieldsList, JoinType.LEFT);
            return tfJoin.get(TwinFieldDataListEntity.Fields.dataListOptionId);
        };
    }

    @Override
    public boolean checkCompatibleCounter(FieldTyper fieldTyper) {
        return fieldTyper instanceof FieldTyperSelect && fieldTyper.getStorageType().equals(TwinFieldStorageDatalist.class);
    }
}
