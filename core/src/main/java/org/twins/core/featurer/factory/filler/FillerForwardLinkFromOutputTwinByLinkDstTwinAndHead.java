package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
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

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Deprecated
@Component
@Featurer(
        id = FeaturerTwins.ID_2351,
        name = "Forward link from output twin by link dst and head twin",
        description = "Finds link in output twin links. " +
                "Get dst twin for this link. " +
                "Get head of this dst twin. " +
                "Create new link of given type from current twin pointing to this head"
)
@Slf4j
public class FillerForwardLinkFromOutputTwinByLinkDstTwinAndHead extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "Head hunter link", description = "", order = 1)
    public static final FeaturerParamUUID firstHopLink = new FeaturerParamUUIDTwinsLinkId("firstHopLink");

    @FeaturerParam(name = "New link id", description = "", order = 2)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        // the link to create is step-constant (same newLinksId for every item) -> resolve it once per batch
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        for (FactoryItem factoryItem : factoryItems) {
            try {
                fillItem(properties, factoryItem, link);
            } catch (Exception ex) {
                if (optional && canBeOptional()) {
                    log.warn("Optional filler step failed for {}, skipping: {}", factoryItem.logShort(), (ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage()));
                } else {
                    throw ex;
                }
            }
        }
    }

    private void fillItem(Properties properties, FactoryItem factoryItem, LinkEntity link) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getTwin();
        if (outputTwin == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output twin is empty");
        }
        if (!(factoryItem.getOutput() instanceof TwinCreate twinCreate)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory output is not TwinCreate");
        }
        TwinEntity dstTwin = getDstTwinByLink(properties, twinCreate, outputTwin);
        TwinEntity detectedHead = twinService.loadHead(dstTwin);
        addLink(factoryItem.getOutput(), TwinLinkEntity.of(link, outputTwin, detectedHead));
    }

    private TwinEntity getDstTwinByLink(Properties properties, TwinCreate twinCreate, TwinEntity outputTwin) throws ServiceException {
        UUID firstHopLinkId = firstHopLink.extract(properties);
        List<TwinLinkEntity> outputTwinLinks = twinCreate.getLinksEntityList();
        if (CollectionUtils.isEmpty(outputTwinLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + firstHopLinkId + "] configured from " + outputTwin.logShort());
        }
        List<TwinLinkEntity> matchedLinks = outputTwinLinks.stream()
                .filter(twinLink -> firstHopLinkId.equals(twinLink.getLinkId()))
                .toList();
        if (CollectionUtils.isEmpty(matchedLinks)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + firstHopLinkId + "] configured from " + outputTwin.logShort());
        }
        if (matchedLinks.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + firstHopLinkId + "] configured from " + outputTwin.logShort());
        }
        twinLinkService.loadDstTwin(matchedLinks);
        return  matchedLinks.getFirst().getDstTwin();
    }
}
