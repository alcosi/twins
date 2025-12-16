package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
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
@Featurer(id = FeaturerTwins.ID_2002,
        name = "Domain scope / business account manage",
        description = "")
public class SluggerDomainScopeBusinessAccountManage extends Slugger<UserGroupMapType2Entity> {
    @Autowired
    UserGroupMapType2Repository userGroupMapType2Repository;

    @Override
    protected boolean checkConfig(Properties properties, UserGroupMapType2Entity userGroupMapEntity) throws ServiceException {
        return check(userGroupMapEntity.getUserGroup(), null, userGroupMapEntity.getBusinessAccountId(), true, false, false, true);
    }

    @Override
    protected List<? extends UserGroupMap> getGroups(Properties properties, Set<UUID> userIds) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return userGroupMapType2Repository.findByUserIdInAndBusinessAccountId(
                userIds,
                apiUser.getBusinessAccountId());
    }

    @Override
    protected UserGroupMap enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        var apiUser = authService.getApiUser();
        if (!apiUser.isBusinessAccountSpecified()) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be entered by userId[" + user + "]. Business account is unknown");
            return null;
        } else if (!checkDomainCompatability(userGroup.getDomainId())) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be entered by userId[" + user + "]");
            return null;
        }
        var ret = new UserGroupMapType2Entity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(user.getId())
                .setUser(user)
                .setBusinessAccountId(apiUser.getBusinessAccountId())
                .setBusinessAccount(apiUser.getBusinessAccount())
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
        userGroupMapType2Repository.save(ret);
        return ret;
    }

    @Override
    protected boolean exitGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        UserGroupMapType2Entity entityToDelete =  userGroupMapType2Repository.findByUserIdAndUserGroupIdAndBusinessAccountId(user.getId(), userGroup.getId(), authService.getApiUser().getBusinessAccountId());
        if (entityToDelete == null)
            return false;
        entitySmartService.deleteAndLog(entityToDelete.getId(), userGroupMapType2Repository);
        return true;
    }

    @Override
    protected void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException {
        //we should delete only members from groups of given type, which where linked to given BA. But group should not be deleted, because it will include users from other BAs
        List<UUID> usersToDelete = userGroupMapType2Repository.findAllByBusinessAccountIdAndDomainIdAndType(businessAccountId, authService.getApiUser().getDomainId(), userGroupTypeEntity.getId());
        entitySmartService.deleteAllAndLog(usersToDelete, userGroupMapType2Repository);
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {
        //todo implement
    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {
        //todo implement
    }

    @Override
    protected Set<UUID> getUsers(Properties properties, UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds) throws ServiceException {
        return userGroupMapType2Repository.findUserIdsByBusinessAccountIdAndUserGroupIds(businessAccountId, userGroupIds);
    }
}
