package org.twins.core.featurer.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4902,
        name = "Context collector twin assignee user",
        description = "")
@Slf4j
public class ContextCollectorTwinAssigneeUser extends ContextCollectorUser<ContextRoot> {

    @Override
    protected UserEntity getUser(ContextRoot root, Properties properties) {
        return root.getTwin().getAssignerUser();
    }
}
