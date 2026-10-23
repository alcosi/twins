package org.twins.core.featurer.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4903,
        name = "Context collector history actor user",
        description = "")
@Slf4j
public class ContextCollectorHistoryActorUser extends ContextCollectorUser {

    @Override
    protected UserEntity getUser(ContextRoot root, Properties properties) {
        if (root.getHistory() == null) {
            log.warn("{} requires a history root, skipping", this.getClass().getSimpleName());
            return null;
        }
        return root.getHistory().getActorUser();
    }
}
