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
        TwinEntity contextTwin = checkNotMultiplySrc(factoryItem);
        factoryItem.getOutputTwin().getTwinEntity()
                .setHeadTwin(contextTwin)
                .setHeadTwinId(contextTwin.getId());
    }
}
