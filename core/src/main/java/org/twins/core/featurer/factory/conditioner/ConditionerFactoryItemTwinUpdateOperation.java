package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2410,
        name = "ConditionerTwinUpdateOperation",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinUpdateOperation extends Conditioner {
    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getOutputTwin() instanceof TwinUpdate;
    }
}
