package org.twins.core.featurer.notificator.recipient;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_47,
        name = "Recipient resolver",
        description = "")
@Slf4j
public abstract class RecipientResolver extends FeaturerTwins {

    /**
     * Batch contract — resolves recipients for a resolver group sharing the same featurer params.
     * {@link RecipientResolveContext#getRecipientIdsByHistory()} is the shared accumulator
     * (history -> recipient ids) owned by the caller; the histories to resolve are exactly its keySet.
     */
    public void resolveBatch(RecipientResolveContext context, HashMap<String, String> recipientParams) throws ServiceException {
        if (context.isEmpty()) {
            return;
        }
        Properties properties = featurerService.extractProperties(this, recipientParams);
        resolveBatch(context, properties);
    }

    public abstract void resolveBatch(RecipientResolveContext context, Properties properties) throws ServiceException;
}
