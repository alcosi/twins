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
    public Set<UUID> resolve(HistoryEntity history, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams, new HashMap<>());
        return resolve(history, properties);
    }

    protected abstract Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException;


}
