package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.*;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2003,
        name = "Business account scope / business account manage",
        description = "")
@RequiredArgsConstructor
public class SluggerBusinessAccountScopeBusinessAccountManage extends Slugger<UserGroupMapType1Entity> {
    @Autowired
    UserGroupMapType1Repository userGroupMapType1Repository;

    @Override
    protected boolean checkConfig(Properties properties, UserGroupMapType1Entity userGroupMapEntity) throws ServiceException {
        return check(userGroupMapEntity.getUserGroup(), false, true);
    }

    @Override
    protected List<? extends UserGroupMap> getGroups(Properties properties, Set<UUID> userIds) throws ServiceException {
        return userGroupMapType1Repository.findByUserIdInAndUserGroup_BusinessAccountId(userIds, authService.getApiUser().getBusinessAccountId());
    }

    @Override
    protected UserGroupMap enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        if (!checkBusinessAccountCompatability(userGroup.getBusinessAccountId())) {
            return null;
        }
        var apiUser = authService.getApiUser();
        var ret = new UserGroupMapType1Entity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(user.getId())
                .setUser(user)
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
        userGroupMapType1Repository.save(ret);
        return ret;
    }

    @Override
    protected boolean exitGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        //todo no need for select, cause data is already loaded in user
        UserGroupMapType1Entity entityToDelete = userGroupMapType1Repository
                .findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountId(user.getId(), userGroup.getId(), authService.getApiUser().getBusinessAccountId());
        if (entityToDelete == null)
            return false;
        entitySmartService.deleteAndLog(entityToDelete.getId(), userGroupMapType1Repository);
        return true;
    }

    @Override
    protected void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException {
        //nothing to do, because slugger should not react on BA from domain deletion
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {
        //nothing to do
    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {
        //todo implement me
    }

    @Override
    protected Set<UUID> getUsers(Properties properties, UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds) throws ServiceException {
        return userGroupMapType1Repository.findUserIdsByUserGroupIdsAndBusinessAccountId(businessAccountId, userGroupIds);
    }
}
