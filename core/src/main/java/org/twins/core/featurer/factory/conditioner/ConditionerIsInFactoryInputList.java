package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2406,
        name = "ConditionerIsInFactoryInputList",
        description = "")
@Slf4j
public class ConditionerIsInFactoryInputList extends Conditioner {
    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        for (TwinEntity factoryInputTwinEntity : factoryItem.getFactoryContext().getInputTwinList()) {
            if (factoryInputTwinEntity.getId().equals(factoryItem.getOutputTwin().getTwinEntity().getId()))
                return true;
        }
        return false;
    }
}
