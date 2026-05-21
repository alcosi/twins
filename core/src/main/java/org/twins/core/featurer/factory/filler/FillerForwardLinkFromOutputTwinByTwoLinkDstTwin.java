package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.jetbrains.annotations.NotNull;
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

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2350,
        name = "Forward link from output twin by first link dst and second link dst twin",
        description = "Find first-hop link in output links. " +
                "Take its dst twin. " +
                "Find second-hop link from that twin via DB. " +
                "Create new link of given type from current twin to second-hop dst twin"
)
public class FillerForwardLinkFromOutputTwinByTwoLinkDstTwin extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinLinkRepository twinLinkRepository;


    @FeaturerParam(name = "First hop link", description = "First-hop link id from output twin links", order = 1)
    public static final FeaturerParamUUID firstHopLink = new FeaturerParamUUIDTwinsLinkId("firstHopLink");

    @FeaturerParam(name = "Second hop link", description = "Second-hop link id via DB from first-hop dst twin", order = 2)
    public static final FeaturerParamUUID secondHopLink = new FeaturerParamUUIDTwinsLinkId("secondHopLink");

    @FeaturerParam(name = "New link id", description = "", order = 3)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.getTwin();
        if (contextTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }
        if (!(factoryItem.getOutput() instanceof TwinCreate twinCreate)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output is not TwinCreate");
        }

        UUID firstHopDstTwinId = getDstTwinIdByFirstLink(properties, twinCreate, contextTwin);

        UUID secondHopDstTwinId = getDstTwinIdBySecondLink(properties, firstHopDstTwinId);

        TwinEntity detectedDstTwin = twinService.findEntitySafe(secondHopDstTwinId);
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        TwinLinkEntity newLink = new TwinLinkEntity()
                .setLink(link)
                .setLinkId(link.getId())
                .setDstTwin(detectedDstTwin)
                .setDstTwinId(detectedDstTwin.getId());
        addLink(factoryItem.getOutput(), newLink);
    }

    @NotNull
    private static UUID getDstTwinIdByFirstLink(Properties properties, TwinCreate twinCreate, TwinEntity contextTwin) throws ServiceException {
        UUID firstHopLinkId = firstHopLink.extract(properties);
        List<TwinLinkEntity> outputTwinLinks = twinCreate.getLinksEntityList();
        if (outputTwinLinks == null || outputTwinLinks.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + firstHopLinkId + "] configured from " + contextTwin.logShort());
        }
        List<TwinLinkEntity> firstHopLinks = outputTwinLinks.stream()
                .filter(twinLink -> firstHopLinkId.equals(twinLink.getLinkId()))
                .toList();
        if (firstHopLinks.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + firstHopLinkId + "] configured from " + contextTwin.logShort());
        }
        if (firstHopLinks.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + firstHopLinkId + "] configured from " + contextTwin.logShort());
        }

        TwinLinkEntity firstHopLinkEntity = firstHopLinks.getFirst();
        UUID firstHopDstTwinId = firstHopLinkEntity.getDstTwinId();
        if (firstHopDstTwinId == null && firstHopLinkEntity.getDstTwin() != null) {
            firstHopDstTwinId = firstHopLinkEntity.getDstTwin().getId();
        }
        if (firstHopDstTwinId == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "First hop link has empty dstTwin and dstTwinId");
        }
        return firstHopDstTwinId;
    }

    @NotNull
    private UUID getDstTwinIdBySecondLink(Properties properties, UUID firstHopDstTwinId) throws ServiceException {
        UUID secondHopLinkId = secondHopLink.extract(properties);
        List<UUID> secondHopDstTwinIds = twinLinkRepository.findDstTwinIdsBySrcTwinIdAndLinkId(firstHopDstTwinId, secondHopLinkId);
        if (secondHopDstTwinIds.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + secondHopLinkId + "] configured from twin[" + firstHopDstTwinId + "]");
        }
        if (secondHopDstTwinIds.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Too many links[" + secondHopLinkId + "] configured from twin[" + firstHopDstTwinId + "]");
        }
        UUID secondHopDstTwinId = secondHopDstTwinIds.getFirst();
        return secondHopDstTwinId;
    }
}
