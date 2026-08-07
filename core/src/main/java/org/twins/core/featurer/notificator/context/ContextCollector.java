package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_49,
        name = "Context collector",
        description = "")
@Slf4j
public abstract class ContextCollector extends FeaturerTwins {

    /**
     * Batch contract — collects context for a group of histories sharing the same featurer params.
     * {@code contextByHistory} is the shared per-history context accumulator (history -> context map) owned by
     * the caller; the histories to collect for are exactly its keySet, so there is no separate history list.
     */
    public void collectDataBatch(Map<HistoryEntity, Map<String, String>> contextByHistory, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams);
        collectDataBatch(contextByHistory, properties);
    }

    public abstract void collectDataBatch(Map<HistoryEntity, Map<String, String>> contextByHistory, Properties properties) throws ServiceException;
}
