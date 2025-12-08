package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@FeaturerType(id = FeaturerTwins.TYPE_49,
        name = "Context collector",
        description = "")
@Slf4j
public abstract class ContextCollector extends FeaturerTwins {

    //todo impl twin type
    protected Map<UUID, String> classMap = new HashMap<>() {{
        put(UUID.fromString("458c6d7d-99c8-4d87-89c6-2f72d0f5d673"), "PROJECT");
        put(UUID.fromString("7c027b60-0f6c-445c-9889-8ee3855d2c59"), "TASK");
        put(UUID.fromString("fd7db1a9-3b0c-42c7-b512-cba1a715049f"), "SUBTASK");
    }};

    public Map<String,String> collect(HistoryEntity history, Map<String, String> context, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams, new HashMap<>());
        return collect(history, context, properties);
    }

    protected abstract Map<String,String> collect(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException;
}
