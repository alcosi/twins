package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamStringTwinOperationLauncher;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2437,
        name = "Check twin operation",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinOperationLauncher extends Conditioner{
    @FeaturerParam(name = "Twin operation", description = "", order = 1)
    public static final FeaturerParamStringTwinOperationLauncher twinOperationLauncher = new FeaturerParamStringTwinOperationLauncher("twinOperationLauncher");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return twinOperationLauncher.extract(properties).equals(factoryItem.getOutput().getLauncher());
    }
}
