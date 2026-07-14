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
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2350,
        name = "Forward link from output twin by two hops",
        description = "Resolves target twin in two hops. " +
                "Each hop uses link id when specified, otherwise head twin. " +
                "Then creates new forward link from output twin to the resolved twin"
)
public class FillerForwardLinkFromOutputTwinByTwoLinkedTwin extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinLinkRepository twinLinkRepository;

    @FeaturerParam(name = "New link id", description = "", order = 1)
    public static final FeaturerParamUUID newLinkId = new FeaturerParamUUIDTwinsLinkId("newLinkId");

    @FeaturerParam(name = "First hop link id", description = "If omitted, first hop resolves output twin head", order = 2, optional = true)
    public static final FeaturerParamUUID firstHopLinkId = new FeaturerParamUUIDTwinsLinkId("firstHopLinkId");

    @FeaturerParam(name = "Second hop link id", description = "If omitted, second hop resolves head of first-hop twin", order = 3, optional = true)
    public static final FeaturerParamUUID secondHopLinkId = new FeaturerParamUUIDTwinsLinkId("secondHopLinkId");

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        fillEach(properties, factoryItems, templateTwin, optional);
    }

    @Override
    protected void fillItem(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }
        if (!(factoryItem.getOutput() instanceof TwinCreate twinCreate)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output is not TwinCreate");
        }

        UUID firstHopLink = firstHopLinkId.extract(properties);
        UUID secondHopLink = secondHopLinkId.extract(properties);

        TwinEntity firstHopDstTwin = firstHopLink != null
                ? getDstTwinByFirstLink(firstHopLink, twinCreate, outputTwin)
                : getHeadTwin(outputTwin);

        TwinEntity finalDstTwin = secondHopLink != null
                ? getDstTwinBySecondLink(secondHopLink, firstHopDstTwin)
                : getHeadTwin(firstHopDstTwin);

        LinkEntity link = linkService.findEntitySafe(newLinkId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setDstTwin(finalDstTwin)
                .setDstTwinId(finalDstTwin.getId());
        addLink(factoryItem.getOutput(), newLink);
    }

    private TwinEntity getHeadTwin(TwinEntity twin) throws ServiceException {
        TwinEntity headTwin = twinService.loadHead(twin);
        if (headTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin configured for " + twin.logShort());
        }
        return headTwin;
    }

    private TwinEntity getDstTwinByFirstLink(UUID linkId, TwinCreate twinCreate, TwinEntity outputTwin) throws ServiceException {
        List<TwinLinkEntity> outputTwinLinks = twinCreate.getLinksEntityList();
        if (CollectionUtils.isEmpty(outputTwinLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + linkId + "] configured from " + outputTwin.logShort());
        }
        List<TwinLinkEntity> firstHopLinks = outputTwinLinks.stream()
                .filter(twinLink -> linkId.equals(twinLink.getLinkId()))
                .toList();
        if (CollectionUtils.isEmpty(firstHopLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + linkId + "] configured from " + outputTwin.logShort());
        }
        if (firstHopLinks.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + linkId + "] configured from " + outputTwin.logShort());
        }
        twinLinkService.loadDstTwin(firstHopLinks);
        TwinLinkEntity firstHopLinkEntity = firstHopLinks.getFirst();
        TwinEntity dstTwin = firstHopLinkEntity.getDstTwin();
        if (dstTwin == null) {
            if (firstHopLinkEntity.getDstTwinId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "First hop link has empty dstTwin and dstTwinId");
            }
            dstTwin = twinService.findEntitySafe(firstHopLinkEntity.getDstTwinId());
        }
        return dstTwin;
    }

    private TwinEntity getDstTwinBySecondLink(UUID linkId, TwinEntity firstHopDstTwin) throws ServiceException {
        List<UUID> secondHopDstTwinIds = twinLinkRepository.findDstTwinIdsBySrcTwinIdAndLinkId(firstHopDstTwin.getId(), linkId);
        if (secondHopDstTwinIds.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + linkId + "] configured from twin[" + firstHopDstTwin.getId() + "]");
        }
        if (secondHopDstTwinIds.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Too many links[" + linkId + "] configured from twin[" + firstHopDstTwin.getId() + "]");
        }
        return twinService.findEntitySafe(secondHopDstTwinIds.getFirst());
    }
}
