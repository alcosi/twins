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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twin.TwinService;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_2325,
        name = "Forward link from context twin link dst twin head",
        description = "Finds link in context twin. " +
                "Get dst twin for this link. " +
                "Get head of this dst twin. " +
                "Create new link of given type from current twin pointing to this head")
public class FillerForwardLinkFromContextTwinLinkDstTwinHead extends FillerLinks {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "Head hunter link", description = "", order = 2)
    public static final FeaturerParamUUID headHunterLink = new FeaturerParamUUIDTwinsLinkId("headHunterLink");

    @FeaturerParam(name = "New links id", description = "", order = 1)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    /**
     * Direct batch override. The old per-item {@code lookupLink} recursion (walk up the context chain,
     * up to 5 levels above the item's own context twin, until a forward link of the configured id is
     * found) is restructured level-major: each walk level pays ONE bulk {@code loadTwinLinks} for all
     * still-pending items instead of one query per item, and the context checks stay isolated per
     * item. After the walk ONE bulk {@code loadDstTwin} and ONE bulk {@code loadHead} cover the whole
     * batch, then the in-memory distribution runs under the same isolation — see
     * featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID headHunterLinkId = headHunterLink.extract(properties);
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties)); // step constant — one lookup per step
        var contextTwinByItem = new LinkedHashMap<FactoryItem, TwinEntity>(); // level-0 twin, for the original error messages
        var pendingItems = new LinkedHashMap<FactoryItem, FactoryItem>(); // original item -> its ancestor item at the current walk level
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                contextTwinByItem.put(factoryItem, factoryItem.checkSingleContextTwin());
                pendingItems.put(factoryItem, factoryItem);
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
        var resolvedLinksByItem = new HashMap<FactoryItem, List<TwinLinkEntity>>();
        int deep = 5;
        while (!pendingItems.isEmpty() && deep >= 0) {
            var failedItems = new ArrayList<FactoryItem>();
            var levelTwins = new ArrayList<TwinEntity>(pendingItems.size());
            for (var entry : pendingItems.entrySet()) {
                try {
                    levelTwins.add(entry.getValue().checkSingleContextTwin());
                } catch (Exception ex) {
                    failedItems.add(entry.getKey());
                    handleItemError(entry.getKey(), optionalStep, ex);
                }
            }
            failedItems.forEach(pendingItems::remove);
            if (!levelTwins.isEmpty())
                twinLinkService.loadTwinLinks(levelTwins); // ONE query for the whole walk level
            var nextPending = new LinkedHashMap<FactoryItem, FactoryItem>();
            for (var iterator = pendingItems.entrySet().iterator(); iterator.hasNext(); ) {
                var entry = iterator.next();
                FactoryItem originalItem = entry.getKey();
                FactoryItem currentItem = entry.getValue();
                try {
                    List<TwinLinkEntity> matchedLinks = currentItem.checkSingleContextTwin().getTwinLinks().getForwardLinks().getGrouped(headHunterLinkId);
                    if (CollectionUtils.isEmpty(matchedLinks))
                        nextPending.put(originalItem, currentItem.checkSingleContextItem()); // descend, validated — same as the old recursion
                    else
                        resolvedLinksByItem.put(originalItem, matchedLinks);
                } catch (Exception ex) {
                    handleItemError(originalItem, optionalStep, ex);
                }
                iterator.remove();
            }
            pendingItems = nextPending;
            deep--;
        }
        for (var iterator = pendingItems.keySet().iterator(); iterator.hasNext(); ) {
            FactoryItem originalItem = iterator.next();
            handleItemError(originalItem, optionalStep, new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "No links[" + headHunterLinkId + "] configured from " + contextTwinByItem.get(originalItem).logShort()));
            iterator.remove();
        }
        var allMatchedLinks = new ArrayList<TwinLinkEntity>();
        for (List<TwinLinkEntity> matchedLinks : resolvedLinksByItem.values())
            allMatchedLinks.addAll(matchedLinks);
        twinLinkService.loadDstTwin(allMatchedLinks); // one query for the whole batch
        var dstTwins = new ArrayList<TwinEntity>(allMatchedLinks.size());
        for (TwinLinkEntity matchedLink : allMatchedLinks)
            dstTwins.add(matchedLink.getDstTwin());
        twinService.loadHead(dstTwins); // one query for the whole batch
        for (Map.Entry<FactoryItem, List<TwinLinkEntity>> entry : resolvedLinksByItem.entrySet()) {
            FactoryItem factoryItem = entry.getKey();
            try {
                List<TwinLinkEntity> matchedLinks = entry.getValue();
                if (matchedLinks.size() != 1)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "To many links[" + headHunterLinkId + "] configured from " + contextTwinByItem.get(factoryItem).logShort());
                TwinEntity detectedHead = matchedLinks.getFirst().getDstTwin().getHeadTwin();
                TwinLinkEntity newLink = new TwinLinkEntity()
                        .setLink(link)
                        .setLinkId(link.getId())
                        .setDstTwin(detectedHead)
                        .setDstTwinId(detectedHead.getId());
                addLink(factoryItem.getOutput(), newLink);
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }
}
