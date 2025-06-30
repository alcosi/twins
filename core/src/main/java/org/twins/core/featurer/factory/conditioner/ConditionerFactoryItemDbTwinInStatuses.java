package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2433,
        name = "Factory item db twin with statuses",
        description = "")
@Slf4j
public class ConditionerFactoryItemDbTwinInStatuses extends Conditioner {

    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        if (factoryItem.getOutput() instanceof TwinUpdate twinUpdate) {
            return statusIds.extract(properties).contains(twinUpdate.getDbTwinEntity().getTwinStatusId());
        }
        return false;
    }
}
