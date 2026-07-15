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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2325,
        name = "Forward link from context twin link dst twin head",
        description = "Finds link in context twin. " +
                "Get dst twin for this link. " +
                "Get head of this dst twin. " +
                "Create new link of given type from current twin pointing to this head")
@Slf4j
public class FillerForwardLinkFromContextTwinLinkDstTwinHead extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "Head hunter link", description = "", order = 2)
    public static final FeaturerParamUUID headHunterLink = new FeaturerParamUUIDTwinsLinkId("headHunterLink");

    @FeaturerParam(name = "New links id", description = "", order = 1)
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
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        List<TwinLinkEntity> contextTwinLinksList = lookupLink(properties, factoryItem, 5);
        if (CollectionUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links[" + headHunterLink.extract(properties) + "] configured from " + contextTwin.logShort());
        if (contextTwinLinksList.size() != 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headHunterLink.extract(properties) + "] configured from " + contextTwin.logShort());
        twinLinkService.loadDstTwin(contextTwinLinksList);
        var dstTwin = contextTwinLinksList.getFirst().getDstTwin();
        twinService.loadHead(dstTwin);
        var detectedHead = dstTwin.getHeadTwin();
        addLink(factoryItem.getOutput(), TwinLinkEntity.of(link, factoryItem.getTwin(), detectedHead));
    }

    //todo optimize with hierarchy
    private List<TwinLinkEntity> lookupLink(Properties properties, FactoryItem factoryItem, int deep) throws ServiceException {
            TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
            List<TwinLinkEntity> contextTwinLinksList = twinLinkService.findTwinForwardLinks(contextTwin)
                    .getGrouped(headHunterLink.extract(properties));
            if (CollectionUtils.isEmpty(contextTwinLinksList) && deep > 0)
                contextTwinLinksList = lookupLink(properties, factoryItem.checkSingleContextItem(), deep - 1);
        return contextTwinLinksList;
    }
}
