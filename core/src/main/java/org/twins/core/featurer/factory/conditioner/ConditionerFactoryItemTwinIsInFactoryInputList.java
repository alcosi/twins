package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2406,
        name = "Factory item twin is in factory input list",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinIsInFactoryInputList extends Conditioner {
    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        for (TwinEntity factoryInputTwinEntity : factoryItem.getFactoryContext().getInputTwinList()) {
            if (factoryInputTwinEntity.getId().equals(factoryItem.getOutput().getTwinEntity().getId()))
                return true;
        }
        return false;
    }
}
