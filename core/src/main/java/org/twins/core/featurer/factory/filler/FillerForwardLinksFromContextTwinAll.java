package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2303,
        name = "Forward links from context twin all",
        description = "")
public class FillerForwardLinksFromContextTwinAll extends FillerLinks {
    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        fillEach(properties, factoryItems, templateTwin, optional);
    }

    @Override
    protected void fillItem(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        Kit<TwinLinkEntity, UUID> contextTwinLinksList = twinLinkService.findTwinForwardLinks(contextTwin);
        if (KitUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No forward links configured from " + contextTwin.logShort());
        addLinks(factoryItem, contextTwinLinksList.getCollection());
    }
}
