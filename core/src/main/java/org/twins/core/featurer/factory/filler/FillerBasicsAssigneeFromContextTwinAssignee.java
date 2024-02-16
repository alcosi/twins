package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;

import java.util.Properties;

@Component
@Featurer(id = 2316,
        name = "FillerBasicsAssigneeFromContextTwinAssignee",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContextTwinAssignee extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        outputTwinEntity
                .setAssignerUser(contextTwin.getAssignerUser())
                .setAssignerUserId(contextTwin.getAssignerUserId());
    }
}
