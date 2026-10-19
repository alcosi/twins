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
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twin.TwinService;

import java.util.*;

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

    /**
     * Direct batch override, two-phase: the matched link is resolved per item purely in memory (the
     * output's own uncommitted links, isolated per item), then ONE bulk {@code loadDstTwin} and ONE
     * bulk {@code loadHead} cover the whole batch, then the in-memory distribution under the same
     * per-item isolation — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID headFromLinkId = headFromLink.extract(properties);
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties)); // step constant — one lookup per step
        var matchedLinksByItem = new LinkedHashMap<FactoryItem, TwinLinkEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                matchedLinksByItem.put(factoryItem, resolveSingleMatchedLink(factoryItem, headFromLinkId));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
        twinLinkService.loadDstTwin(matchedLinksByItem.values()); // one query for the whole batch
        var dstTwins = new ArrayList<TwinEntity>(matchedLinksByItem.size());
        for (TwinLinkEntity matchedLink : matchedLinksByItem.values())
            dstTwins.add(matchedLink.getDstTwin());
        twinService.loadHead(dstTwins); // one query for the whole batch
        for (Map.Entry<FactoryItem, TwinLinkEntity> entry : matchedLinksByItem.entrySet()) {
            FactoryItem factoryItem = entry.getKey();
            try {
                TwinEntity outputTwin = factoryItem.getTwin();
                TwinEntity detectedHead = entry.getValue().getDstTwin().getHeadTwin();
                TwinLinkEntity newLink = new TwinLinkEntity()
                        .setLink(link)
                        .setLinkId(link.getId())
                        .setSrcTwinId(outputTwin.getId())
                        .setSrcTwin(outputTwin)
                        .setDstTwin(detectedHead)
                        .setDstTwinId(detectedHead.getId());
                addLink(factoryItem.getOutput(), newLink);
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }

    /** Exactly one link of the configured id on the output twin's uncommitted links. */
    private TwinLinkEntity resolveSingleMatchedLink(FactoryItem factoryItem, UUID headFromLinkId) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        List<TwinLinkEntity> outputLinks = ((TwinCreate) factoryItem.getOutput()).getLinksEntityList();
        if (CollectionUtils.isEmpty(outputLinks))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headFromLinkId + "] configured from " + outputTwin.logShort());
        List<TwinLinkEntity> matchedLinks = outputLinks.stream()
                .filter(twinLink -> headFromLinkId.equals(twinLink.getLinkId()))
                .toList();
        if (CollectionUtils.isEmpty(matchedLinks))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headFromLinkId + "] configured from " + outputTwin.logShort());
        if (matchedLinks.size() != 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headFromLinkId + "] configured from " + outputTwin.logShort());
        return matchedLinks.getFirst();
    }
}
