package org.twins.core.featurer.notificator.recipient;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;


@FeaturerType(id = FeaturerTwins.TYPE_47,
        name = "Recipient resolver",
        description = "")
@Slf4j
public abstract class RecipientResolver extends FeaturerTwins {

    /**
     * Batch contract — resolves recipients for a group of histories sharing the same featurer params.
     * {@code recipientIdsByHistory} is the shared accumulator (history -> recipient ids) owned by the caller;
     * the histories to resolve are exactly its keySet, so there is no separate history list to keep in sync.
     */
    public void resolveBatch(Map<HistoryEntity, Set<UUID>> recipientIdsByHistory, HashMap<String, String> recipientParams) throws ServiceException {
        if (recipientIdsByHistory.isEmpty()) {
            return;
        }
        Properties properties = featurerService.extractProperties(this, recipientParams);
        resolveBatch(recipientIdsByHistory, properties);
    }

    public abstract void resolveBatch(Map<HistoryEntity, Set<UUID>> recipientIdsByHistory, Properties properties) throws ServiceException;
}
