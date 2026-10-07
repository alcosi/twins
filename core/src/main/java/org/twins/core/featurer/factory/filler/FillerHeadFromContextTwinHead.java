package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2302,
        name = "Head from context twin head",
        description = "Walks the context twin head hierarchy N levels up (see 'depth' param) and sets the result as the new twin's head. ")
public class FillerHeadFromContextTwinHead extends Filler {

    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "Depth head twin", description = "How many levels up the head hierarchy to walk. 1 = head of the context twin (default)", optional = true, defaultValue = "1")
    public static final FeaturerParamInt depthHeadTwin = new FeaturerParamInt("depth");

    /**
     * Direct batch override, two-phase: the head ids are resolved per item purely in memory
     * (hierarchyTree walk, isolated per item — the agreement check can fail an individual item),
     * then ONE bulk {@code findEntitiesSafe} covers the whole batch, then the in-memory
     * distribution — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        int depth = depthHeadTwin.extract(properties);
        if (depth < 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "head depth must be >= 1, got: " + depth);
        }
        var detectedHeadTwinIds = new LinkedHashMap<FactoryItem, UUID>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                detectedHeadTwinIds.put(factoryItem, detectSingleHeadTwinId(factoryItem, depth));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
        var headTwinKit = twinService.findEntitiesSafe(detectedHeadTwinIds.values()); // one query for the whole batch
        for (Map.Entry<FactoryItem, UUID> entry : detectedHeadTwinIds.entrySet())
            TwinHeadService.setHead(entry.getKey().getOutput().getTwinEntity(), headTwinKit.get(entry.getValue()));
    }

    /** All context twins of the item must resolve to the same head at the given depth. */
    private UUID detectSingleHeadTwinId(FactoryItem factoryItem, int depth) throws ServiceException {
        UUID detectedHeadTwinId = null;
        for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) { // we will check if all context twins resolve to the same head at the given depth, otherwise exception
            UUID resolvedHeadTwinId = TwinHeadService.resolveHeadTwinId(contextItem.getTwin(), depth);
            if (detectedHeadTwinId == null) {
                detectedHeadTwinId = resolvedHeadTwinId;
            } else if (!detectedHeadTwinId.equals(resolvedHeadTwinId)) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "multiple head twin context");
            }
        }
        // single lookup for the agreed head: hierarchyTree gave us the id above without any db queries
        if (detectedHeadTwinId == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "no head twin context");
        }
        return detectedHeadTwinId;
    }

    @Override
    public boolean canBeOptional() {
        return false;
    }
}
