package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2301,
        name = "HeadAsContextTwin",
        description = "")
public class FillerHeadAsContextTwin extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        factoryItem.getOutput().getTwinEntity()
                .setHeadTwin(contextTwin)
                .setHeadTwinId(contextTwin.getId());
    }

    @Override
    public boolean canBeOptional() {
        return false;
    }
}
