package org.twins.core.featurer.factory.conditioner;


import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2434,
        name = "Check factory launcher type",
        description = "")
@Slf4j
public class ConditionerContextFactoryLauncher extends Conditioner {
    @FeaturerParam(name = "Factory launcher type", description = "", order = 1)
    public static final FeaturerParamString factoryLauncherType = new FeaturerParamString("factoryLauncherType");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return factoryLauncherType.extract(properties).equalsIgnoreCase(factoryItem.getFactoryContext().getFactoryLauncher().name());
    }
}
