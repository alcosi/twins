package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = FeaturerTwins.TYPE_20,
        name = "Slugger",
        description = "")
@Slf4j
public abstract class Slugger extends FeaturerTwins {
    @Lazy
    @Autowired
    AuthService authService;

    @Lazy
    @Autowired
    EntitySmartService entitySmartService;

    @Lazy
    @Autowired
    UserGroupService userGroupService;

    public void enterGroup(UserGroupEntity userGroup, UserEntity user) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroup.getUserGroupType().getSluggerParams());
        userGroupService.loadGroups(user);
        if (user.getUserGroups().containsKey(userGroup.getId())) {
            log.warn("{} is already registered in {}", user.logShort(), userGroup.logShort());
            return;
        }
        UserGroupMapEntity userGroupMapEntity = enterGroup(properties, user, userGroup);
        if (userGroupMapEntity == null) {
            log.warn("{} is not allowed for user[{}]", userGroup.logNormal(), user);
        } else
            user.getUserGroups().add(userGroup);
    }

    protected abstract UserGroupMapEntity enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException;

    public void exitGroup(UserGroupEntity userGroup, UserEntity user) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroup.getUserGroupType().getSluggerParams());
        userGroupService.loadGroups(user);
        if (!user.getUserGroups().containsKey(userGroup.getId())) {
            log.warn("{} is not registered in {}", user.logShort(), userGroup.logShort());
            return;
        }
        if (!exitGroup(properties, user, userGroup))
            log.warn("{} can not be exited by user[{}]", userGroup.logNormal(), user);
        else
            user.getUserGroups().remove(userGroup); //check me
    }

    protected abstract boolean exitGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException;

    public void processDomainBusinessAccountDeletion(UserGroupTypeEntity userGroupTypeEntity, UUID businessAccountId) throws ServiceException {
        Properties properties = extractProperties(userGroupTypeEntity.getSluggerParams(), true);
        processDomainBusinessAccountDeletion(properties, businessAccountId, userGroupTypeEntity);
    }

    protected abstract void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException;

    public void processDomainDeletion(HashMap<String, String> sluggerParams) throws ServiceException {
        Properties properties = extractProperties(sluggerParams, true);
        processDomainDeletion(properties);
    }

    protected abstract void processDomainDeletion(Properties properties) throws ServiceException;

    public void processBusinessAccountDeletion(HashMap<String, String> sluggerParams) throws ServiceException {
        Properties properties = extractProperties(sluggerParams, true);
        processBusinessAccountDeletion(properties);
    }

    protected abstract void processBusinessAccountDeletion(Properties properties) throws ServiceException;

    protected boolean checkDomainCompatability(UUID domainId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return apiUser.isDomainSpecified() &&
                domainId != null &&
                domainId.equals(apiUser.getDomainId());
    }

    protected boolean checkBusinessAccountCompatability(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return apiUser.isBusinessAccountSpecified() &&
                businessAccountId != null &&
                businessAccountId.equals(apiUser.getBusinessAccountId());
    }
}
