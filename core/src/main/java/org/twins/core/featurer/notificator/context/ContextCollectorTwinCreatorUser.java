package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4901,
        name = "Context collector twin creator user",
        description = "")
@Slf4j
public class ContextCollectorTwinCreatorUser extends ContextCollectorUser {

    @Override
    protected UserEntity getUser(HistoryEntity history, Properties properties) {
        return history.getTwin().getCreatedByUser();
    }
}
