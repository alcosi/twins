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
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsLinkId;

import java.util.Collection;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2305,
        name = "Forward links from context twin",
        description = "Copies the context twin's forward links onto the output twin. "
                + "The optional linksIds param filters the copied links by link id; when empty, ALL forward links are copied")
public class FillerForwardLinksFromContextTwin extends FillerLinks {

    @FeaturerParam(name = "Links ids", description = "Empty = all forward links", order = 1, optional = true)
    public static final FeaturerParamUUIDSet linksIds = new FeaturerParamUUIDSetTwinsLinkId("linksIds");

    /**
     * Direct batch override: ONE bulk {@code loadTwinLinks} over the batch context twins (which also
     * bulk-loads the dst twins and link entities), then the per-item in-memory read (optionally
     * filtered by linksIds) under isolation — the per-item {@code addLinks} loads short-circuit on
     * the already-loaded entities — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        var extractedLinksIds = linksIds.extract(properties); // empty set when absent -> no filtering
        if (!batch.getContextTwins().isEmpty())
            twinLinkService.loadTwinLinks(batch.getContextTwins()); // one query for the whole batch
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
                Collection<TwinLinkEntity> contextTwinLinksList = contextTwin.getTwinLinks().getForwardLinks().getCollection();
                if (!extractedLinksIds.isEmpty())
                    contextTwinLinksList = contextTwinLinksList.stream()
                            .filter(twinLink -> extractedLinksIds.contains(twinLink.getLinkId()))
                            .toList();
                if (CollectionUtils.isEmpty(contextTwinLinksList))
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                            extractedLinksIds.isEmpty()
                                    ? "No forward links configured from " + contextTwin.logShort()
                                    : "No links[" + extractedLinksIds + "] configured from " + contextTwin.logShort());
                addLinks(factoryItem, contextTwinLinksList);
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }
}
