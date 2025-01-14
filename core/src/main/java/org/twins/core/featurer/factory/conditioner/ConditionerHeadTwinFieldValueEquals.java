package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2424,
        name = "HeadTwinFieldValueEquals",
        description = "")
@Slf4j
public class ConditionerHeadTwinFieldValueEquals extends ConditionerContextValueEquals {

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, fieldLookupers.getFromContextTwinHeadTwinDbFields());
    }
}
