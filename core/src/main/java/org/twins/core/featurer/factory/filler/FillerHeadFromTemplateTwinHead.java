package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2306,
        name = "HeadFromTemplateTwinHead",
        description = "")
public class FillerHeadFromTemplateTwinHead extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (templateTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Empty template twin");
        if (templateTwin.getHeadTwinId() == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Empty template head twin");
        factoryItem.getOutput().getTwinEntity()
                .setHeadTwin(templateTwin.getHeadTwin())
                .setHeadTwinId(templateTwin.getHeadTwinId());
    }
}
