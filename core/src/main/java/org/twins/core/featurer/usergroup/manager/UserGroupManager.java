package org.twins.core.featurer.usergroup.manager;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.ApiUser;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = 21,
        name = "UserGroupManager",
        description = "")
@Slf4j
public abstract class UserGroupManager extends Featurer {
    public void manageForUser(HashMap<String, String> params, UUID userId, List<UUID> userGroupEnterList, List<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        manageForUser(properties, userId, userGroupEnterList, userGroupExitList, apiUser);
    }

    public abstract void manageForUser(Properties properties, UUID userId, List<UUID> userGroupEnterList, List<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException;
}
