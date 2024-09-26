package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2329,
        name = "FillerBasicsAssigneeFromOutputTwinHeadAssignee",
        description = "Fill the assignee from own head twin assignee")
@Slf4j
public class FillerBasicsAssigneeFromOutputTwinHeadAssignee extends Filler {

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinEntity headTwin = outputTwinEntity.getHeadTwin();
        if(null == headTwin)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for twin: " + outputTwinEntity.getId());
        outputTwinEntity
                .setAssignerUser(headTwin.getAssignerUser())
                .setAssignerUserId(headTwin.getAssignerUserId());
    }
}
