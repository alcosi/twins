package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.filler.FieldLookupMode;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2421,
        name = "ConditionerContextTwinFieldValueEquals",
        description = "")
@Slf4j
public class ConditionerOutputTwinFieldValueEquals extends ConditionerContextValueEquals {
    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, FieldLookupMode.fromItemOutputFields);
    }
}
