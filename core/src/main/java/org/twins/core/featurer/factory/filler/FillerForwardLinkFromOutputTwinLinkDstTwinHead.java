package org.twins.core.featurer.factory.filler;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2349,
        name = "Forward link from output twin link dst twin head",
        description = "Finds link in output twin. " +
                "Get dst twin for this link. " +
                "Get head of this dst twin. " +
                "Create new link of given type from current twin pointing to this head")
public class FillerForwardLinkFromOutputTwinLinkDstTwinHead extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "Head form link", description = "", order = 2)
    public static final FeaturerParamUUID headFromLink = new FeaturerParamUUIDTwinsLinkId("headFromLink");

    @FeaturerParam(name = "New links id", description = "", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        List<TwinLinkEntity> contextTwinLinksList = ((TwinCreate) factoryItem.getOutput()).getLinksEntityList();
        UUID headFromLinkId = headFromLink.extract(properties);
        if (CollectionUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headFromLinkId + "] configured from " + outputTwin.logShort());

        List<TwinLinkEntity> matchedLinks = contextTwinLinksList.stream()
                .filter(twinLink -> headFromLinkId.equals(twinLink.getLinkId()))
                .toList();
        if (CollectionUtils.isEmpty(matchedLinks))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headFromLinkId + "] configured from " + outputTwin.logShort());
        if (matchedLinks.size() != 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headFromLinkId + "] configured from " + outputTwin.logShort());

        TwinEntity detectedHead = twinService.loadHeadForTwin(matchedLinks.getFirst().getDstTwin());
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setSrcTwinId(outputTwin.getId())
                .setSrcTwin(outputTwin)
                .setDstTwin(detectedHead)
                .setDstTwinId(detectedHead.getId());
        addLink(factoryItem.getOutput(), newLink);
    }
}
