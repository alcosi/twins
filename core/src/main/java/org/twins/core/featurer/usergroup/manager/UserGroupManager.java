package org.twins.core.featurer.usergroup.manager;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.user.UserGroupService;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;


@FeaturerType(id = FeaturerTwins.TYPE_21,
        name = "UserGroupManager",
        description = "")
@Slf4j
public abstract class UserGroupManager extends FeaturerTwins {
    @Lazy
    @Autowired
    protected UserGroupService userGroupService;

    public void manageForUser(HashMap<String, String> params, UserEntity user, Set<UUID> userGroupEnterList, Set<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params);
        manageForUser(properties, user, userGroupEnterList, userGroupExitList, apiUser);
    }

    public abstract void manageForUser(Properties properties, UserEntity user, Set<UUID> userGroupEnterList, Set<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException;
}
