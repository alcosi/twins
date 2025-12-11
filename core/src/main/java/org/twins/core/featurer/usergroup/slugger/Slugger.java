package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMap;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;

import java.util.*;


@FeaturerType(id = FeaturerTwins.TYPE_20,
        name = "Slugger",
        description = "")
@Slf4j
public abstract class Slugger<T extends UserGroupMap> extends FeaturerTwins {
    @Lazy
    @Autowired
    AuthService authService;

    @Lazy
    @Autowired
    EntitySmartService entitySmartService;

    @Lazy
    @Autowired
    UserGroupService userGroupService;

    public boolean checkConfig(T userGroupMapEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroupMapEntity.getUserGroup().getUserGroupType().getSluggerParams(), new HashMap<>());
        return checkConfig(properties, userGroupMapEntity);
    }

    protected abstract boolean checkConfig(Properties properties, T userGroupMapEntity) throws ServiceException;

    protected boolean check(UserGroupEntity userGroupEntity,
                            boolean userGroupDomainIdRequired,
                            boolean userGroupBusinessAccountIdIdRequired) throws ServiceException {
        return check(userGroupEntity, null, null, userGroupDomainIdRequired, userGroupBusinessAccountIdIdRequired, false, false);
    }

    protected boolean check(UserGroupEntity userGroupEntity, UUID mapDomainId, UUID mapBusinessAccountId,
                            boolean userGroupDomainIdRequired,
                            boolean userGroupBusinessAccountIdIdRequired,
                            boolean mapDomainIdRequired,
                            boolean mapBusinessAccountIdRequired) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        boolean valid = true;
        if (userGroupEntity.getBusinessAccountId() == null) {
            if (userGroupBusinessAccountIdIdRequired) {
                log.warn("{} incorrect config. Group is {}. Business account should be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
                valid = false;
            }
        } else if (userGroupBusinessAccountIdIdRequired) {
            if (!checkBusinessAccountCompatability(userGroupEntity.getBusinessAccountId())) {
                log.warn("{} is not reachable from current business account", userGroupEntity.logNormal());
                valid = false;
            }
        } else {
            log.warn("{} incorrect config. Group is {}. Business account can not be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
            userGroupEntity
                    .setBusinessAccountId(null)
                    .setBusinessAccount(null);
        }

        if (userGroupEntity.getDomainId() == null) {
            if (userGroupDomainIdRequired) {
                log.warn("{} incorrect config. Group is {}. Domain should be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
                valid = false;
            }
        } else if (userGroupDomainIdRequired) {
            if (!checkDomainCompatability(userGroupEntity.getDomainId())) {
                log.warn("{} is not reachable from current domain", userGroupEntity.logNormal());
                valid = false;
            }
        } else {
            log.warn("{} incorrect config. Group is {}. Domain can not be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
            userGroupEntity
                    .setDomainId(null)
                    .setDomain(null);
        }

        if (mapBusinessAccountId == null) {
            if (mapBusinessAccountIdRequired) {
                log.warn("{} incorrect config. Group is {}. Business account should be specified in user_group_map", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
                valid = false;
            }
        } else if (mapBusinessAccountIdRequired) {
            if (!checkBusinessAccountCompatability(mapBusinessAccountId)) {
                log.warn("{} is not reachable from current business account", userGroupEntity.logNormal());
                valid = false;
            }
        }

        if (mapDomainId == null) {
            if (mapDomainIdRequired) {
                log.warn("{} incorrect config. Group is {}. Domain should be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
                valid = false;
            }
        } else if (mapDomainIdRequired) {
            if (!checkDomainCompatability(mapDomainId)) {
                log.warn("{} is not reachable from current domain", userGroupEntity.logNormal());
                valid = false;
            }
        }

        return valid;
    }

    protected boolean checkUserGroupBusinessAccountEmpty(UserGroupEntity userGroupEntity) {
        if (userGroupEntity.getBusinessAccountId() == null)
            return true;
        log.warn("{} incorrect config. Group is {}. Business account can not be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
        userGroupEntity
                .setBusinessAccountId(null)
                .setBusinessAccount(null);
        return false;

    }

    protected boolean checkUserGroupBusinessAccountNotEmpty(UserGroupEntity userGroupEntity) {
        if (userGroupEntity.getBusinessAccountId() != null)
            return true;
        log.warn("{} incorrect config. Group is {}. Business account should be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
        return false;

    }

    protected boolean checkUserGroupDomainEmpty(UserGroupEntity userGroupEntity) {
        if (userGroupEntity.getDomainId() == null)
            return true;
        log.warn("{} incorrect config. Group is {}. Domain can not be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
        userGroupEntity
                .setDomainId(null)
                .setDomain(null);
        return false;
    }

    protected boolean checkUserGroupDomainNotEmpty(UserGroupEntity userGroupEntity) {
        if (userGroupEntity.getDomainId() != null)
            return true;
        log.warn("{} incorrect config. Group is {}. Domain should be specified in user_group", userGroupEntity.logNormal(), userGroupEntity.getUserGroupTypeId());
        return false;
    }

    public List<? extends UserGroupMap> getGroups(HashMap<String, String> sluggerParams, Set<UUID> userIds) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, sluggerParams, new HashMap<>());
        return getGroups(properties, userIds);
    }

    protected abstract List<? extends UserGroupMap> getGroups(Properties properties, Set<UUID> userIds) throws ServiceException;


    public void enterGroup(UserGroupEntity userGroup, UserEntity user) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroup.getUserGroupType().getSluggerParams(), new HashMap<>());
        userGroupService.loadGroups(user);
        if (user.getUserGroups().containsKey(userGroup.getId())) {
            log.warn("{} is already registered in {}", user.logShort(), userGroup.logShort());
            return;
        }
        UserGroupMap userGroupMapEntity = enterGroup(properties, user, userGroup);
        if (userGroupMapEntity == null) {
            log.warn("{} is not allowed for user[{}]", userGroup.logNormal(), user);
        } else
            user.getUserGroups().add(userGroup);
    }

    protected abstract UserGroupMap enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException;

    public void exitGroup(UserGroupEntity userGroup, UserEntity user) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userGroup.getUserGroupType().getSluggerParams(), new HashMap<>());
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

    public Set<UUID> getUsers(HashMap<String, String> sluggerParams, UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, sluggerParams, new HashMap<>());
        return getUsers(properties, domainId, businessAccountId, userGroupIds);
    }

    protected abstract Set<UUID> getUsers(Properties properties, UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds) throws ServiceException;

}
