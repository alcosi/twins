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

    public Map<String, String> collectData(HistoryEntity history, Map<String, String> context, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams);
        return collectData(history, context, properties);
    }

    protected abstract Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException;
}
