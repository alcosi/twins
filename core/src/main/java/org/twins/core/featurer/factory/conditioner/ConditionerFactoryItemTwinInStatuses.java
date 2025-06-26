package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2432,
        name = "Factory item twin with statuses",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinInStatuses extends Conditioner {

    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return statusIds.extract(properties).contains(factoryItem.getOutput().getTwinEntity().getTwinStatusId());
    }
}
