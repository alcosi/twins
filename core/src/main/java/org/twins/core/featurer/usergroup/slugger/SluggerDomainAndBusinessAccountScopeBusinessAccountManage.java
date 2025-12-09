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
@Featurer(id = FeaturerTwins.ID_2004,
        name = "Domain and business account scope / business account manage",
        description = "")
@RequiredArgsConstructor
public class SluggerDomainAndBusinessAccountScopeBusinessAccountManage extends Slugger<UserGroupMapType1Entity> {
    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    UserGroupMapType1Repository userGroupMapType1Repository;

    @Override
    protected boolean checkConfig(Properties properties, UserGroupMapType1Entity userGroupMapEntity) throws ServiceException {
        return check(userGroupMapEntity.getUserGroup(), true, true);
    }

    @Override
    protected List<? extends UserGroupMap> getGroups(Properties properties, Set<UUID> userIds) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return userGroupMapType1Repository.findByUserIdInAndUserGroup_BusinessAccountIdAndUserGroup_DomainId(
                userIds,
                apiUser.getBusinessAccountId(),
                apiUser.getDomainId());
    }

    @Override
    protected UserGroupMap enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        if (!checkBusinessAccountCompatability(userGroup.getBusinessAccountId())) {
            log.warn("{} can not be entered by {}", userGroup.logNormal(), user);
            return null;
        }
        if (!checkDomainCompatability(userGroup.getDomainId())) {
            log.warn("{} can not be entered by {}", userGroup.logNormal(), user);
            return null;
        }
        var apiUser = authService.getApiUser();
        return new UserGroupMapType1Entity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(user.getId())
                .setUser(user)
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
    }

    @Override
    protected boolean exitGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        //todo no need for select, cause data is already loaded in user
        var apiUser = authService.getApiUser();
        UserGroupMapType1Entity entityToDelete = userGroupMapType1Repository
                .findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountIdAndUserGroup_DomainId(user.getId(), userGroup.getId(), apiUser.getBusinessAccountId(), apiUser.getDomainId());
        if (entityToDelete == null)
            return false;
        entitySmartService.deleteAndLog(entityToDelete.getId(), userGroupMapType1Repository);
        return true;
    }

    @Override
    protected void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException {
        //we should delete groups of given type, because they were created only for given BA in given domain
        //no need to delete data from user_group_map table, because it has cascade delete FK on user_group table
        List<UUID> groupsToDelete = userGroupRepository.findAllByBusinessAccountIdAndDomainIdAndType(businessAccountId, authService.getApiUser().getDomainId(), userGroupTypeEntity.getId());
        entitySmartService.deleteAllAndLog(groupsToDelete, userGroupRepository);
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {

    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {

    }

    @Override
    protected Set<UUID> getUsers(Properties properties, UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds) throws ServiceException {
        return userGroupMapType1Repository.findUserIdsByUserGroupIdsAndDomainIdAndBusinessAccountId(domainId, businessAccountId, userGroupIds);
    }


}
