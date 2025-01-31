package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2420,
        name = "Factory context twin of class",
        description = "")
@Slf4j
public class ConditionerFactoryContextTwinOfClass extends ConditionerFactoryContextTwinOfClassDeep {

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        for (var fi : factoryItem.getFactoryContext().getFactoryItemList()) {
            if (check(properties, fi, 1))
                return true;
        }
        return false;
    }
}
