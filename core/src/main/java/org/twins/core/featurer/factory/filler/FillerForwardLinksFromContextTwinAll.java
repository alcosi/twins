package org.twins.core.featurer.factory.filler;

import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

@Component
@Featurer(id = 2303,
        name = "FillerForwardLinksFromContextTwinAll",
        description = "")
public class FillerForwardLinksFromContextTwinAll extends FillerLinks {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        Kit<TwinLinkEntity> contextTwinLinksList = twinLinkService.findTwinForwardLinks(contextTwin);
        if (KitUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No forward links configured from " + contextTwin.logShort());
        addLinks(factoryItem, contextTwinLinksList.getList());
    }
}
