package org.twins.core.featurer.twin.counter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5401, name = "No grouping", description = "Counter stub — no meaningful grouping applied")
public class TwinCounterStub extends TwinCounter {
    @Override
    public GroupExpressionProvider<TwinEntity> createGroup(Properties properties, TwinClassFieldEntity twinClassFieldEntity) {
        return (root, query, cb) -> cb.nullLiteral(Object.class);
    }

    @Override
    public boolean checkCompatibleCounter(FieldTyper fieldTyper) {
        return true;
    }
}
