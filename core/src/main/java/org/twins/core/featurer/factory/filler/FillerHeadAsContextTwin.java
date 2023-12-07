package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

@Component
@Featurer(id = 2301,
        name = "FillerHeadAsContextTwin",
        description = "")
public class FillerHeadAsContextTwin extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (factoryItem.getContextTwinList().size() != 0)
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "context twin size != 1. Please check multiplier");
        TwinEntity contextTwin = factoryItem.getContextTwinList().get(0);
        factoryItem.getOutputTwin().getTwinEntity()
                .setHeadTwin(contextTwin)
                .setHeadTwinId(contextTwin.getId());
    }
}
