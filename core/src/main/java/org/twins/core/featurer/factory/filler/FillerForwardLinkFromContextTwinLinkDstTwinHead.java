package org.twins.core.featurer.factory.filler;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 2325,
        name = "FillerForwardLinkFromContextTwinLinkDstTwinHead",
        description = "Finds link in context twin. " +
                "Get dst twin for this link. " +
                "Get head of this dst twin. " +
                "Create new link of given type from current twin pointing to this head")
public class FillerForwardLinkFromContextTwinLinkDstTwinHead extends FillerLinks {
    @FeaturerParam(name = "headHunterLink", description = "")
    public static final FeaturerParamUUID headHunterLink = new FeaturerParamUUID("headHunterLink");

    @FeaturerParam(name = "newLinksId", description = "")
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUID("newLinksId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin(); //todo support deep item search logic
        List<TwinLinkEntity> contextTwinLinksList = twinLinkService.findTwinForwardLinks(contextTwin)
                .getGrouped(headHunterLink.extract(properties));
        if (CollectionUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headHunterLink.extract(properties) + "] configured from " + contextTwin.logShort());
        if (contextTwinLinksList.size() != 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headHunterLink.extract(properties) + "] configured from " + contextTwin.logShort());
        TwinEntity detectedHead = contextTwinLinksList.get(0).getDstTwin().getHeadTwin();
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setDstTwin(detectedHead)
                .setDstTwinId(detectedHead.getId());
        addLink(factoryItem.getOutput(), newLink);
    }
}
