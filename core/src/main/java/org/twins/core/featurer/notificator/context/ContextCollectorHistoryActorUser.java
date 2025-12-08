package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.ID_4904,
        name = "Context collector history actor user",
        description = "")
@Slf4j
public class ContextCollectorHistoryActorUser extends ContextCollectorUser {
    @Override
    protected Map<String, String> collect(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        collect(history, context, properties);
        return context;
    }
}
