package org.twins.core.featurer.factory.filler;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsLinkId;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 2305,
        name = "FillerForwardLinksFromContextTwin",
        description = "")
public class FillerForwardLinksFromContextTwin extends FillerLinks {
    @FeaturerParam(name = "linksIds", description = "")
    public static final FeaturerParamUUIDSet linksIds = new FeaturerParamUUIDSetTwinsLinkId("linksIds");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        List<TwinLinkEntity> contextTwinLinksList = twinLinkService.findTwinForwardLinks(contextTwin, linksIds.extract(properties));
        if (CollectionUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + linksIds.extract(properties) + "] configured from " + contextTwin.logShort());
        addLinks(factoryItem, contextTwinLinksList);
    }
}
