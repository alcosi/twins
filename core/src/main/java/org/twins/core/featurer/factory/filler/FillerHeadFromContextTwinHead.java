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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.Set;
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

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        int depth = depthHeadTwin.extract(properties);
        if (depth < 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "head depth must be >= 1, got: " + depth);
        }
        UUID detectedHeadTwinId = null;
        for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) { // we will check if all context twins resolve to the same head at the given depth, otherwise exception
            UUID resolvedHeadTwinId = resolveHeadTwinId(contextItem.getTwin(), depth);
            if (detectedHeadTwinId == null) {
                detectedHeadTwinId = resolvedHeadTwinId;
            } else if (!detectedHeadTwinId.equals(resolvedHeadTwinId)) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "multiple head twin context");
            }
        }
        // single lookup for the agreed head: hierarchyTree gave us the id above without any db queries
        TwinEntity detectedHeadTwin = detectedHeadTwinId == null ? null : twinService.findHeadTwin(detectedHeadTwinId);
        factoryItem.getOutput().getTwinEntity()
                .setHeadTwin(detectedHeadTwin)
                .setHeadTwinId(detectedHeadTwinId);
    }

    private UUID resolveHeadTwinId(TwinEntity twin, int depth) {
        Set<UUID> headTwinsIdSet = twin.getHeadTwinsIdSet();
        if (headTwinsIdSet != null) {
            if (headTwinsIdSet.size() <= depth) {
                return null; // hierarchy is shorter than depth + 1 -> no head at this depth
            }
            int index = 0;
            for (UUID id : headTwinsIdSet) { // LinkedHashSet keeps leaf-first order
                if (index == depth) {
                    return id;
                }
                index++;
            }
            return null;
        }
        // hierarchyTree is not loaded -> walk the head links as a fallback
        TwinEntity current = twin;
        for (int i = 0; i < depth; i++) {
            if (current == null) {
                return null;
            }
            TwinEntity head = current.getHeadTwin();
            if (head == null && current.getHeadTwinId() != null) {
                head = twinService.findHeadTwin(current.getHeadTwinId());
            }
            current = head;
        }
        return current == null ? null : current.getId();
    }

    @Override
    public boolean canBeOptional() {
        return false;
    }
}
