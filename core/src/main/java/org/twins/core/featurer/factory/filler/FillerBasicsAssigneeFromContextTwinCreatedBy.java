package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2313,
        name = "Basics assignee from context twin created by",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContextTwinCreatedBy extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        outputTwinEntity
                .setAssignerUser(contextTwin.getCreatedByUser())
                .setAssignerUserId(contextTwin.getCreatedByUserId());
    }
}
