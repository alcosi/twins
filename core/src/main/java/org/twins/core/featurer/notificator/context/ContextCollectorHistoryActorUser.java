package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@FeaturerType(id = FeaturerTwins.ID_4905,
        name = "Context collector history actor user",
        description = "")
@Slf4j
public class ContextCollectorHistoryActorUser extends ContextCollectorUser {

    @Override
    protected UserEntity getUser(HistoryEntity history, Properties properties) {
        return history.getActorUser();
    }
}
