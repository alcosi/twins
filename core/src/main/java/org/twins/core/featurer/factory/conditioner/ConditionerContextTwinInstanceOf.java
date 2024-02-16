package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2411,
        name = "ConditionerContextTwinInstanceOf",
        description = "")
@Slf4j
public class ConditionerContextTwinInstanceOf extends ConditionerContextTwinInstanceOfDeep {
    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, 1);  //we do not need to go deep
    }
}
