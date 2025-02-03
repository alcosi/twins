package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2329,
        name = "Basics assignee from head twin assignee",
        description = "Fill the assignee from own head twin assignee")
@Slf4j
public class FillerBasicsAssigneeFromOutputTwinHeadAssignee extends Filler {

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity factoryItemTwin = factoryItem.getTwin();
        TwinEntity headTwin = twinService.loadHeadForTwin(factoryItemTwin);
        if(null == headTwin)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for twin: " + factoryItemTwin.logDetailed());
        factoryItem.getOutput().getTwinEntity()
                .setAssignerUser(headTwin.getAssignerUser())
                .setAssignerUserId(headTwin.getAssignerUserId());
    }
}
