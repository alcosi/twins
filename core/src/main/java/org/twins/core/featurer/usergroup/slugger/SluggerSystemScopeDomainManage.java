package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2005,
        name = "System scope / domain manage",
        description = "")
@RequiredArgsConstructor
public class SluggerSystemScopeDomainManage extends Slugger<UserGroupMapType3Entity> {
    @Autowired
    UserGroupMapType3Repository userGroupMapType3Repository;

    @Override
    protected boolean checkConfig(Properties properties, UserGroupMapType3Entity userGroupMapEntity) throws ServiceException {
        return check(userGroupMapEntity.getUserGroup(), userGroupMapEntity.getDomainId(), null, false, false, true, false);
    }

    @Override
    protected List<? extends UserGroupMap> getGroups(Properties properties, Set<UUID> userIds) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return userGroupMapType3Repository.findByUserIdInAndDomainId(
                userIds,
                apiUser.getDomainId());
    }

    @Override
    protected UserGroupMap enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        var apiUser = authService.getApiUser();
        if (!apiUser.isDomainSpecified()) {
            log.warn("{} can not be entered by userId[{}]", userGroup.logNormal(), user);
            return null;
        }
        var ret = new UserGroupMapType3Entity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(user.getId())
                .setUser(user)
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()))
                .setDomainId(apiUser.getDomainId())
                .setDomain(apiUser.getDomain());
        userGroupMapType3Repository.save(ret);
        return ret;
    }

    @Override
    protected boolean exitGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        var apiUser = authService.getApiUser();
        UserGroupMapType3Entity entityToDelete = userGroupMapType3Repository.findByUserIdAndUserGroupIdAndDomainId(user.getId(), userGroup.getId(), apiUser.getDomainId());
        if (entityToDelete == null)
            return false;
        entitySmartService.deleteAndLog(entityToDelete.getId(), userGroupMapType3Repository);
        return true;
    }

    @Override
    protected void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException {
        //nothing to do
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {
        //todo implement
    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {
        //nothing to do
    }

    @Override
    protected Set<UUID> getUsers(Properties properties, UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds) throws ServiceException {
        return userGroupMapType3Repository.findUserIdsByDomainIdIdAndUserGroupIds(domainId, userGroupIds);
    }
}
