package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@FeaturerType(id = FeaturerTwins.ID_4903,
        name = "Context collector twin assignee user",
        description = "")
@Slf4j
public class ContextCollectorTwinAssigneeUser extends ContextCollectorUser {
    @Override
    protected UserEntity getUser(HistoryEntity history, Properties properties) {
        return history.getTwin().getAssignerUser();
    }
}
