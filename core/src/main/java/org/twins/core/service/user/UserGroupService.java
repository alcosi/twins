package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;
import org.twins.core.featurer.usergroup.slugger.Slugger;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupService {
    final EntitySmartService entitySmartService;
    final UserGroupRepository userGroupRepository;
    final UserGroupTypeRepository userGroupTypeRepository;
    final UserGroupMapRepository userGroupMapRepository;
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;

    public List<UserGroupEntity> findGroupsForUser(UUID userId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UserGroupEntity> userGroupEntityList = new ArrayList<>();
        List<UserGroupMapEntity> userGroupMapEntityList = getUserGroupsMap(userId);
        for (UserGroupMapEntity userGroupMapEntity : userGroupMapEntityList) {
            Slugger slugger = featurerService.getFeaturer(userGroupMapEntity.getUserGroup().getUserGroupType().getSluggerFeaturer(), Slugger.class);
            UserGroupEntity userGroup = slugger.checkConfigAndGetGroup(userGroupMapEntity);
            if (userGroup != null)
                userGroupEntityList.add(userGroup);
        }
        return userGroupEntityList;
    }

    public Set<UUID> loadGroups(ApiUser apiUser) throws ServiceException {
        if (apiUser.getUserGroups() != null)
            return apiUser.getUserGroups();
        List<UserGroupMapEntity> userGroupMapEntityList = getUserGroupsMap(apiUser.getUserId());
        if (CollectionUtils.isNotEmpty(userGroupMapEntityList))
            apiUser.setUserGroups(new HashSet<>());
        for (UserGroupMapEntity userGroupMapEntity : userGroupMapEntityList) {
            Slugger slugger = featurerService.getFeaturer(userGroupMapEntity.getUserGroup().getUserGroupType().getSluggerFeaturer(), Slugger.class);
            UserGroupEntity userGroup = slugger.checkConfigAndGetGroup(userGroupMapEntity);
            if (userGroup != null)
                apiUser.getUserGroups().add(userGroup.getId());
        }
        return apiUser.getUserGroups();
    }

    public List<UserGroupMapEntity> getUserGroupsMap(UUID userId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.isBusinessAccountSpecified())
            return userGroupMapRepository.findByUserIdAndBusinessAccountSafe(userId, apiUser.getDomain().getId(), apiUser.getBusinessAccountId());
        else
            return userGroupMapRepository.findByUserIdAndBusinessAccountSafe(userId, apiUser.getDomain().getId());
    }


    public void manageForUser(UUID userId, List<UUID> userGroupEnterList, List<UUID> userGroupExitList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainEntity domainEntity = apiUser.getDomain();
        UserGroupManager userGroupManager = featurerService.getFeaturer(domainEntity.getUserGroupManagerFeaturer(), UserGroupManager.class);
        userGroupManager.manageForUser(domainEntity.getUserGroupManagerParams(), userId, userGroupEnterList, userGroupExitList, apiUser);
    }

    public void forceDeleteUserGroups(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UserGroupTypeEntity> types = userGroupTypeRepository.findAll();
        if (CollectionUtils.isEmpty(types))
            return;
        UserGroupEntity userGroup = new UserGroupEntity()
                .setDomainId(apiUser.getDomainId())
                .setBusinessAccountId(businessAccountId);
        for (UserGroupTypeEntity type : types) {
            userGroup.setUserGroupTypeId(type.getId());
            Slugger slugger = featurerService.getFeaturer(type.getSluggerFeaturer(), Slugger.class);
            slugger.deleteDomainBusinessAccount(type.getSluggerParams(), userGroup);
            slugger.deleteDomain(type.getSluggerParams(), userGroup);
            slugger.deleteBusinessAccount(type.getSluggerParams(), userGroup);
        }
        //method to delete user
//        List<UUID> groupsToDelete = userGroupRepository.findAllByBusinessAccountIdAndDomainIdAndType(businessAccountId, domainId, "domainAndBusinessAccountScopeBusinessAccountManage");
//        entitySmartService.deleteAllAndLog(groupsToDelete, userGroupRepository);
    }
}
