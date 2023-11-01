package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupMapRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = 20,
        name = "Slugger",
        description = "")
@Slf4j
public abstract class Slugger extends Featurer {
    @Autowired
    UserGroupMapRepository userGroupMapRepository;
    @Lazy
    @Autowired
    AuthService authService;

    public UserGroupEntity checkConfigAndGetGroup(UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroupMapEntity.getUserGroup().getUserGroupType().getSluggerParams(), new HashMap<>());
        return checkConfigAndGetGroup(properties, userGroupMapEntity);
    }

    protected abstract UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException;

    protected void checkUserGroupBusinessAccountEmpty(UserGroupEntity userGroupEntity) {
        if (userGroupEntity.getBusinessAccountId() != null) {
            log.warn(userGroupEntity + " incorrect config. Group is " + userGroupEntity.getUserGroupTypeId() + ". Business account can not be specified in user_group");
            userGroupEntity
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
    public void enterGroup(UserGroupEntity userGroup, UUID userId) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroup.getUserGroupType().getSluggerParams(), new HashMap<>());
        UserGroupMapEntity userGroupMapEntity = userGroupMapRepository.findByUserIdAndUserGroupId(userId, userGroup.getId());
        if (userGroupMapEntity != null) {
            log.warn(userGroupMapEntity.easyLog(EasyLoggable.Level.NORMAL) + " is already exists");
            return;
        }
        userGroupMapEntity = enterGroup(properties, userGroup, userId, authService.getApiUser());
        if (userGroupMapEntity == null) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for user[" + userId + "]");
            return;
        }
        userGroupMapRepository.save(userGroupMapEntity);
    }
    protected abstract UserGroupMapEntity enterGroup(Properties properties, UserGroupEntity userGroup, UUID userId, ApiUser apiUser) throws ServiceException;


    public void exitGroup(UserGroupEntity userGroup, UUID userId) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroup.getUserGroupType().getSluggerParams(), new HashMap<>());
        UserGroupMapEntity userGroupMapEntity = userGroupMapRepository.findByUserIdAndUserGroupId(userId, userGroup.getId());
        if (userGroupMapEntity == null) {
            log.warn("UserGroup[" + userGroup + " is not entered by user[" + userId + "]");
            return;
        }
        if (!exitGroup(properties, userGroupMapEntity, authService.getApiUser()))
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be exited by user[" + userId + "]");
    }

    protected boolean exitGroup(Properties properties, UserGroupMapEntity userGroupMapEntity, ApiUser apiUser) {
        userGroupMapRepository.delete(userGroupMapEntity);
        return true;
    }
}
