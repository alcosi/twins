package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;
import org.twins.core.featurer.usergroup.slugger.Slugger;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupService extends EntitySecureFindServiceImpl<UserGroupEntity> {
    final UserGroupRepository userGroupRepository;
    final UserGroupTypeRepository userGroupTypeRepository;
    final UserGroupActAsUserInvolveRepository actAsUserInvolveRepository;
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;
    @Lazy
    final UserService userService;

    @Override
    public CrudRepository<UserGroupEntity, UUID> entityRepository() {
        return userGroupRepository;
    }

    @Override
    public Function<UserGroupEntity, UUID> entityGetIdFunction() {
        return UserGroupEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(UserGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Kit<UserGroupEntity, UUID> findGroupsForUser(UUID userId) throws ServiceException {
        UserEntity userEntity = userService.findEntitySafe(userId);
        loadGroups(userEntity);
        return userEntity.getUserGroups();
    }

    public void loadGroupsForCurrentUser() throws ServiceException {
        loadGroups(authService.getApiUser().getUser());
    }

    public void loadGroups(UserEntity userEntity) throws ServiceException {
        loadGroups(Collections.singletonList(userEntity));
    }

    public void loadGroups(Collection<UserEntity> userEntityList) throws ServiceException {
        Kit<UserEntity, UUID> needLoad = new Kit<>(UserEntity::getId);
        ApiUser apiUser = authService.getApiUser();
        boolean actAsUserInvolving = apiUser.getUser().getUserGroups() == null && apiUser.isMachineUserSpecified();
        for (UserEntity userEntity : userEntityList) {
            if (userEntity.getUserGroups() == null) {
                userEntity.setUserGroups(new Kit<>(UserGroupEntity::getId));
                needLoad.add(userEntity);
            }
        }
        if (CollectionUtils.isEmpty(needLoad))
            return;

        UUID businessAccountId = apiUser.isBusinessAccountSpecified() ? apiUser.getBusinessAccountId() : ApiUser.NOT_SPECIFIED; // this will help to call next query
        List<UserGroupTypeEntity> userGroupTypes = userGroupTypeRepository.findValidTypes(apiUser.getDomainId(), businessAccountId);
        if (CollectionUtils.isEmpty(userGroupTypes))
            return;
        List<? extends UserGroupMap> userGroups;
        for (UserGroupTypeEntity userGroupTypeEntity : userGroupTypes) {
            Slugger<UserGroupMap> slugger = featurerService.getFeaturer(userGroupTypeEntity.getSluggerFeaturer(), Slugger.class);
            userGroups = slugger.getGroups(userGroupTypeEntity.getSluggerParams(), needLoad.getIdSet());
            if (CollectionUtils.isNotEmpty(userGroups))
                for (var userGroupMap : userGroups) {
                    if (slugger.checkConfig(userGroupMap))
                        needLoad.get(userGroupMap.getUserId()).getUserGroups().add(userGroupMap.getUserGroup());
                }
        }
        if (actAsUserInvolving) {
            userGroupsForActAsUserInvolve();
        }
    }

    private void userGroupsForActAsUserInvolve() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!apiUser.isMachineUserSpecified()) {
            return;
        }
        UserEntity actAsUser = apiUser.getUser();
        List<UserGroupActAsUserInvolveEntity> actAsUserInvolveList = actAsUserInvolveRepository.findByMachineUserIdAndDomainId(apiUser.getMachineUserId(), apiUser.getDomainId());
        if (CollectionUtils.isEmpty(actAsUserInvolveList)) {
            log.debug("Current machine user has not act as user involve");
            return;
        }
        actAsUser.getUserGroups().addAll(actAsUserInvolveList.stream().map(UserGroupActAsUserInvolveEntity::getInvolveInUserGroup).toList());
    }

    public void enterGroups(Set<UUID> userGroupIds) throws ServiceException {
        manageForUser(authService.getApiUser().getUserId(), userGroupIds, null);
    }

    public void enterGroup(UUID userGroupId) throws ServiceException {
        manageForUser(authService.getApiUser().getUserId(), Collections.singleton(userGroupId), null);
    }

    public void enterGroup(UUID userId, UUID userGroupId) throws ServiceException {
        manageForUser(userId, Collections.singleton(userGroupId), null);
    }

    public void exitGroup(UUID userGroupId) throws ServiceException {
        manageForUser(authService.getApiUser().getUserId(), null, Collections.singleton(userGroupId));
    }

    public void exitGroup(UUID userId, UUID userGroupId) throws ServiceException {
        manageForUser(userId, null, Collections.singleton(userGroupId));
    }

    public void manageForUser(UUID userId, Set<UUID> userGroupEnterList, Set<UUID> userGroupExitList) throws ServiceException {
        manageForUser(userService.findEntitySafe(userId), userGroupEnterList, userGroupExitList);
    }

    public void manageForUser(UserEntity user, Set<UUID> userGroupEnterList, Set<UUID> userGroupExitList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainEntity domainEntity = apiUser.getDomain();
        UserGroupManager userGroupManager = featurerService.getFeaturer(domainEntity.getUserGroupManagerFeaturer(), UserGroupManager.class);
        userGroupManager.manageForUser(domainEntity.getUserGroupManagerParams(), user, userGroupEnterList, userGroupExitList, apiUser);
    }

    public void processDomainBusinessAccountDeletion(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UserGroupTypeEntity> types = userGroupTypeRepository.findAll();
        if (CollectionUtils.isEmpty(types))
            return;
        for (UserGroupTypeEntity type : types) {
            Slugger slugger = featurerService.getFeaturer(type.getSluggerFeaturer(), Slugger.class);
            slugger.processDomainBusinessAccountDeletion(type, businessAccountId);
        }
    }


}
