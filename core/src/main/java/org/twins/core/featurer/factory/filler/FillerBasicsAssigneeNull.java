package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2317,
        name = "FillerBasicsAssigneeNull",
        description = "")
@Slf4j
public class FillerBasicsAssigneeNull extends Filler {

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutputTwin().getTwinEntity();
        outputTwinEntity
                .setAssignerUser(null)
                .setAssignerUserId(factoryItem.getOutputTwin().nullifyUUID());
    }
}
