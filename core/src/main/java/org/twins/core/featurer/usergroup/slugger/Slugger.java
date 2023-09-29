package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 20,
        name = "Slugger",
        description = "")
@Slf4j
public abstract class Slugger extends Featurer {
    public UserGroupEntity checkConfigAndGetGroup(UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroupMapEntity.getUserGroup().getUserGroupType().getSluggerParams(), new HashMap<>());
        return checkConfigAndGetGroup(properties, userGroupMapEntity);
    }

    protected abstract UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException;

    protected void checkUserGroupBusinessAccountEmpty(UserGroupMapEntity userGroupMapEntity) {
        if (userGroupMapEntity.getUserGroup().getBusinessAccountId() != null) {
            log.warn(userGroupMapEntity.getUserGroup() + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Business account can not be specified in user_group");
            userGroupMapEntity.getUserGroup()
                    .setBusinessAccountId(null)
                    .setBusinessAccount(null);
        }
    }

    protected void checkUserGroupMapBusinessAccountEmpty(UserGroupMapEntity userGroupMapEntity) {
        if (userGroupMapEntity.getBusinessAccountId() != null) {
            log.warn(userGroupMapEntity.getUserGroup() + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Business account can not be specified in user_group_map");
            userGroupMapEntity
                    .setBusinessAccountId(null)
                    .setBusinessAccount(null);
        }
    }
}
