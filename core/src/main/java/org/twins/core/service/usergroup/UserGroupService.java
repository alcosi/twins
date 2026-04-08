package org.twins.core.service.usergroup;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.*;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.dao.usergroup.UserGroupMapRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.usergroup.UserGroupCreate;
import org.twins.core.domain.usergroup.UserGroupUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;
import org.twins.core.featurer.usergroup.slugger.Slugger;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserGroupService extends EntitySecureFindServiceImpl<UserGroupEntity> {
    final UserGroupRepository userGroupRepository;
    final UserGroupTypeRepository userGroupTypeRepository;
    final UserGroupMapRepository userGroupMapRepository;
    final UserGroupInvolveActAsUserService userGroupInvolveActAsUserService;
    final FeaturerService featurerService;
    final I18nService i18nService;
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
        return isDomainAccessDenied(entity.getDomainId());
    }

    @Override
    public boolean validateEntity(UserGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        // validation by trigger DB
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
        for (UserEntity userEntity : userEntityList) {
            if (userEntity.getUserGroups() == null) {
                userEntity.setUserGroups(new Kit<>(UserGroupEntity::getId));
                needLoad.add(userEntity);
            }
        }
        if (CollectionUtils.isEmpty(needLoad))
            return;

        List<UserGroupMapEntity> userGroups = userGroupMapRepository.getGroups(apiUser.getDomainId(), apiUser.getBusinessAccountId(), needLoad.getIdSet());
        if (CollectionUtils.isEmpty(userGroups)) {
            return;
        }
        for (var userGroupMap : userGroups) {
            needLoad.get(userGroupMap.getUserId()).getUserGroups().add(userGroupMap.getUserGroup());
        }
        userGroupsForActAsUserInvolve();
    }

    private void userGroupsForActAsUserInvolve() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.getActAsUserStep() != ApiUser.ActAsUserStep.USER_GROUP_INVOLVE_NEEDED) {
            return;
        }
        UserEntity actAsUser = apiUser.getUser();
        var involvedInGroups = userGroupInvolveActAsUserService.findByMachineUserIdAndDomainId(apiUser.getMachineUserId(), apiUser.getDomainId());
        if (CollectionUtils.isEmpty(involvedInGroups)) {
            log.info("Current machine user has not act as user involve");
            return;
        }
        actAsUser.getUserGroups().addAll(involvedInGroups);
        log.info("Act-as-user was involved into: {}", involvedInGroups.size());
        apiUser.setActAsUserStep(ApiUser.ActAsUserStep.READY);
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
        UserGroupManager userGroupManager = featurerService.getFeaturer(domainEntity.getUserGroupManagerFeaturerId(), UserGroupManager.class);
        userGroupManager.manageForUser(domainEntity.getUserGroupManagerParams(), user, userGroupEnterList, userGroupExitList, apiUser);
    }

    public void processDomainBusinessAccountDeletion(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UserGroupTypeEntity> types = userGroupTypeRepository.findAll();
        if (CollectionUtils.isEmpty(types))
            return;
        for (UserGroupTypeEntity type : types) {
            Slugger slugger = featurerService.getFeaturer(type.getSluggerFeaturerId(), Slugger.class);
            slugger.processDomainBusinessAccountDeletion(type, businessAccountId);
        }
    }

    public Set<UUID> getUsersForGroups(UUID domainId, UUID businessAccountId, Set<UUID> userGroupIds) throws ServiceException {
        return userGroupMapRepository.getUsers(domainId, businessAccountId, userGroupIds);
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<UserGroupEntity> createUserGroup(Collection<UserGroupCreate> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        i18nService.createI18nAndTranslations(I18nType.USER_GROUP_NAME,
                entities.stream().map(UserGroupCreate::getNameI18n)
                        .toList());
        i18nService.createI18nAndTranslations(I18nType.USER_GROUP_DESCRIPTION,
                entities.stream().map(UserGroupCreate::getDescriptionI18n)
                        .toList());

        List<UserGroupEntity> entitiesToSave = new ArrayList<>();

        for (UserGroupCreate userGroup : entities) {
            UserGroupEntity entity = new UserGroupEntity();
            entity.setUserGroupTypeId(userGroup.getUserGroupTypeId())
                    .setDomainId(authService.getApiUser().getDomainId())
                    //todo think about permissions for user
                    //.setBusinessAccountId(userGroup.getBusinessAccountId())
                    .setNameI18NId(userGroup.getNameI18n() != null ? userGroup.getNameI18n().getId() : null)
                    .setDescriptionI18NId(userGroup.getDescriptionI18n() != null ? userGroup.getDescriptionI18n().getId() : null);
            entitiesToSave.add(entity);
        }

        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<UserGroupEntity> updateUserGroup(Collection<UserGroupUpdate> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<UserGroupEntity> changes = new ChangesHelperMulti<>();
        Kit<UserGroupEntity, UUID> entitiesKit = findEntitiesSafe(entities.stream().map(UserGroupUpdate::getId).toList());
        List<UserGroupEntity> allEntities = new ArrayList<>(entities.size());

        for (UserGroupUpdate userGroup : entities) {
            UserGroupEntity entity = entitiesKit.get(userGroup.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(userGroup.getNameI18n(), I18nType.USER_GROUP_NAME, entity,
                    UserGroupEntity::getNameI18NId, UserGroupEntity::setNameI18NId, UserGroupEntity.Fields.nameI18N, changesHelper);
            i18nService.updateI18nFieldForEntity(userGroup.getDescriptionI18n(), I18nType.USER_GROUP_DESCRIPTION, entity,
                    UserGroupEntity::getDescriptionI18NId, UserGroupEntity::setDescriptionI18NId, UserGroupEntity.Fields.descriptionI18N, changesHelper);
            // todo for future, as at create
            // updateEntityFieldByValue(entity.getBusinessAccountId(), entity, UserGroupEntity::getBusinessAccountId, UserGroupEntity::setBusinessAccountId, UserGroupEntity.Fields.businessAccountId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);
        return allEntities;
    }
}
