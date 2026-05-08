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
@Featurer(
        id = FeaturerTwins.ID_2351,
        name = "Forward link from output TwinCreate link dst twin head",
        description = "Finds link in output TwinCreate links. " +
                "Get dst twin for this link. " +
                "Get head of this dst twin. " +
                "Create new link of given type from current twin pointing to this head"
)
public class FillerForwardLinkFromOutputTwinLinkDstTwinHeadByTwin extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "First hop link", description = "", order = 2)
    public static final FeaturerParamUUID headHunterLink = new FeaturerParamUUIDTwinsLinkId("headHunterLink");

    @FeaturerParam(name = "New link id", description = "", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }
        if (!(factoryItem.getOutput() instanceof TwinCreate twinCreate)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output is not TwinCreate");
        }
        List<TwinLinkEntity> outputTwinLinks = twinCreate.getLinksEntityList();
        if (CollectionUtils.isEmpty(outputTwinLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headHunterLink.extract(properties) + "] configured from " + outputTwin.logShort());
        }
        UUID headHunterLinkId = headHunterLink.extract(properties);
        List<TwinLinkEntity> matchedLinks = outputTwinLinks.stream()
                .filter(twinLink -> headHunterLinkId.equals(twinLink.getLinkId()))
                .toList();
        if (CollectionUtils.isEmpty(matchedLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headHunterLink.extract(properties) + "] configured from " + outputTwin.logShort());
        }
        if (matchedLinks.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headHunterLink.extract(properties) + "] configured from " + outputTwin.logShort());
        }
        TwinLinkEntity matchedLink = matchedLinks.getFirst();
        TwinEntity dstTwin = matchedLink.getDstTwin();
        if (dstTwin == null) {
            if (matchedLink.getDstTwinId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Matched link has empty dstTwin and dstTwinId");
            }
            dstTwin = twinService.findEntitySafe(matchedLink.getDstTwinId());
        }

        TwinEntity detectedHead = twinService.loadHeadForTwin(dstTwin);
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setDstTwin(detectedHead)
                .setDstTwinId(detectedHead.getId());
        addLink(factoryItem.getOutput(), newLink);
    }
}
