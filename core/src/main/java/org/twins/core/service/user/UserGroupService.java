package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupMapRepository;
import org.twins.core.dao.user.UserGroupRepository;
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
    final UserGroupMapRepository userGroupMapRepository;
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;

    public List<UserGroupEntity> findGroupsForUser(UUID userId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UserGroupEntity> userGroupEntityList = new ArrayList<>();
        List<UserGroupMapEntity> userGroupMapEntityList = userGroupMapRepository.findByUserIdAndUserGroup_DomainId(userId, apiUser.getDomain().getId());
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
        List<UserGroupMapEntity> userGroupMapEntityList = userGroupMapRepository.findByUserIdAndUserGroup_DomainId(apiUser.getUser().getId(), apiUser.getDomain().getId());
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


    public void manageForUser(UUID userId, List<UUID> userGroupEnterList, List<UUID> userGroupExitList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainEntity domainEntity = apiUser.getDomain();
        UserGroupManager userGroupManager = featurerService.getFeaturer(domainEntity.getUserGroupManagerFeaturer(), UserGroupManager.class);
        userGroupManager.manageForUser(domainEntity.getUserGroupManagerParams(), userId, userGroupEnterList, userGroupExitList, apiUser);
    }

    public void forceDeleteUserGroups(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> groupsToDelete = userGroupRepository.findAllByBusinessAccountIdAndDomainIdAndType(businessAccountId, domainId, "domainAndBusinessAccountScopeBusinessAccountManage");
        entitySmartService.deleteAllAndLog(groupsToDelete, userGroupRepository);
    }

    public void forceDeleteUsers(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> usersToDelete = userGroupMapRepository.findAllByBusinessAccountIdAndDomainIdAndTypes(businessAccountId, domainId, List.of("domainScopeBusinessAccountManage", "domainAndBusinessAccountScopeBusinessAccountManage"));
        entitySmartService.deleteAllAndLog(usersToDelete, userGroupMapRepository);
    }
}
