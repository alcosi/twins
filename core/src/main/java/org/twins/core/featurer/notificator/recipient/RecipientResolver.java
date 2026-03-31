package org.twins.core.featurer.notificator.recipient;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;


@FeaturerType(id = FeaturerTwins.TYPE_47,
        name = "Recipient resolver",
        description = "")
@Slf4j
public abstract class RecipientResolver extends FeaturerTwins {
    public void resolve(HistoryEntity history, Set<UUID> recipientIds, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams);
        resolve(history, recipientIds, properties);
    }

    protected abstract void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException;


}
