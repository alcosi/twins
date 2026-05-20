package org.twins.core.featurer.factory.filler;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
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
        name = "Forward link from output twin by (head/link) twin and head twin",
        description = "Resolve intermediate twin via link or head. " +
                "Get head of intermediate twin. " +
                "Create new link of given type from current twin pointing to this head"
)
public class FillerForwardLinkFromOutputTwinByHeadOrLinkDstTwinAndHeadByTwin extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "Head hunter link", description = "", order = 2)
    public static final FeaturerParamUUID headHunterLink = new FeaturerParamUUIDTwinsLinkId("headHunterLink");

    @FeaturerParam(name = "New link id", description = "", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @FeaturerParam(name = "Head else link", description = "", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean headElseLink = new FeaturerParamBoolean("headElseLink");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }
        if (!(factoryItem.getOutput() instanceof TwinCreate)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output is not TwinCreate");
        }

        TwinEntity intermediateTwin = resolveIntermediateTwin(properties, outputTwin, (TwinCreate) factoryItem.getOutput());
        TwinEntity detectedHead = twinService.loadHeadForTwin(intermediateTwin);
        if (detectedHead == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin configured for " + intermediateTwin.logShort());
        }
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setDstTwin(detectedHead)
                .setDstTwinId(detectedHead.getId());
        addLink(factoryItem.getOutput(), newLink);
    }

    private TwinEntity resolveIntermediateTwin(Properties properties, TwinEntity outputTwin, TwinCreate twinCreate) throws ServiceException {
        if (!headElseLink.extract(properties)) {
            TwinEntity headTwin = twinService.loadHeadForTwin(outputTwin);
            if (headTwin == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin configured for " + outputTwin.logShort());
            }
            return headTwin;
        }

        UUID headHunterLinkId = headHunterLink.extract(properties);
        List<TwinLinkEntity> outputTwinLinks = twinCreate.getLinksEntityList();
        if (CollectionUtils.isEmpty(outputTwinLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headHunterLinkId + "] configured from " + outputTwin.logShort());
        }
        List<TwinLinkEntity> matchedLinks = outputTwinLinks.stream()
                .filter(twinLink -> headHunterLinkId.equals(twinLink.getLinkId()))
                .toList();
        if (CollectionUtils.isEmpty(matchedLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headHunterLinkId + "] configured from " + outputTwin.logShort());
        }
        if (matchedLinks.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headHunterLinkId + "] configured from " + outputTwin.logShort());
        }
        TwinLinkEntity matchedLink = matchedLinks.getFirst();
        TwinEntity dstTwin = matchedLink.getDstTwin();
        if (dstTwin == null) {
            if (matchedLink.getDstTwinId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Matched link has empty dstTwin and dstTwinId");
            }
            dstTwin = twinService.findEntitySafe(matchedLink.getDstTwinId());
        }
        return dstTwin;
    }
}
