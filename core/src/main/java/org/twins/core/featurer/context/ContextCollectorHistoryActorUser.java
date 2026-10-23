package org.twins.core.featurer.context;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4903,
        name = "Context collector history actor user",
        description = "")
public class ContextCollectorHistoryActorUser extends ContextCollectorUser<ContextRootHistory> {

    @Override
    protected UserEntity getUser(ContextRootHistory root, Properties properties) {
        return root.getHistory().getActorUser();
    }
}
