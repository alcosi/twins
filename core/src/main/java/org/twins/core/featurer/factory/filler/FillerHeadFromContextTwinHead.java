package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2302,
        name = "FillerHeadFromContextTwinHead",
        description = "")
public class FillerHeadFromContextTwinHead extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID detectedHeadTwinId = null;
        TwinEntity detectedHeadTwin = null;
        for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) { // we will check if all context twins have the save headTwinId, otherwise exception
            TwinEntity contextTwin = contextItem.getTwin();
            if (detectedHeadTwinId == null) {
                detectedHeadTwinId = contextTwin.getHeadTwinId();
                detectedHeadTwin = contextTwin.getHeadTwin();
            }
            if (detectedHeadTwinId != null && !detectedHeadTwinId.equals(contextTwin.getHeadTwinId())) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "multiple head twin context");
            }
        }

        factoryItem.getOutput().getTwinEntity()
                .setHeadTwin(detectedHeadTwin)
                .setHeadTwinId(detectedHeadTwinId);
    }

    @Override
    public boolean canBeOptional() {
        return false;
    }
}
