package org.twins.core.featurer.notificator.notifier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_48,
        name = "Notifier",
        description = "")
@Slf4j
public abstract class Notifier extends FeaturerTwins {

    //todo params some connection config

    public void notify(HistoryEntity history, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams, new HashMap<>());
        notify(history, properties);
    }

    protected abstract void notify(HistoryEntity history, Properties properties) throws ServiceException;
}
