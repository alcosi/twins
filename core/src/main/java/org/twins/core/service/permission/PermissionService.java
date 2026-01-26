package org.twins.core.service.permission;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KeyUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.permission.*;
import org.twins.core.dao.space.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.twin.TwinRole;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.space.SpaceUserRoleService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionService extends TwinsEntitySecureFindService<PermissionEntity> {

    private final PermissionRepository permissionRepository;
    private final PermissionSchemaRepository permissionSchemaRepository;
    private final PermissionGrantUserRepository permissionGrantUserRepository;
    private final PermissionGrantUserGroupRepository permissionGrantUserGroupRepository;
    private final PermissionGrantGlobalRepository permissionGrantGlobalRepository;
    private final PermissionGrantTwinRoleRepository permissionGrantTwinRoleRepository;
    private final PermissionGrantSpaceRoleRepository permissionGrantSpaceRoleRepository;
    private final PermissionGrantAssigneePropagationRepository permissionGrantAssigneePropagationRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceUserRoleService spaceUserRoleService;
    private final SpaceRoleUserGroupRepository spaceRoleUserGroupRepository;
    private final I18nService i18nService;

    private final TwinRepository twinRepository;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final DomainService domainService;
    private final DomainBusinessAccountService domainBusinessAccountService;
    private final UserGroupService userGroupService;
    @Lazy
    private final UserService userService;
    @Lazy
    private final EntitySmartService entitySmartService;
    private final PermissionGroupService permissionGroupService;
    private final ApiUser apiUser;

    @Override
    public CrudRepository<PermissionEntity, UUID> entityRepository() {
        return permissionRepository;
    }

    @Override
    public boolean isEntityReadDenied(PermissionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public Function<PermissionEntity, UUID> entityGetIdFunction() {
        return PermissionEntity::getId;
    }

    @Override
    public BiFunction<UUID, String, Optional<PermissionEntity>> findByDomainIdAndKeyFunction() throws ServiceException {
        //todo that is not uniq safe! domain_id should be also added to permission entity
        return permissionRepository::findByPermissionGroup_DomainIdAndKey;
    }

    @Override
    public boolean validateEntity(PermissionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getKey() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty key");
        if (entity.getPermissionGroupId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionGroupId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionGroup() == null || !entity.getPermissionGroup().getId().equals(entity.getPermissionGroupId()))
                    entity.setPermissionGroup(permissionGroupService.findEntitySafe(entity.getPermissionGroupId()));
                if (entity.getPermissionGroup().getDomainId() == null)
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " is system group. No permission can be added to such group");
                return permissionGroupService.validateEntity(entity.getPermissionGroup(), EntitySmartService.EntityValidateMode.afterRead); // this will check if domain correct
            case afterRead:
                return permissionGroupService.validateEntity(entity.getPermissionGroup(), EntitySmartService.EntityValidateMode.afterRead);
        }
        return true;
    }

    public UUID checkPermissionSchemaAllowed(UUID domainId, UUID businessAccountId, UUID permissionSchemaId) throws ServiceException {
        Optional<PermissionSchemaEntity> permissionSchemaEntity = permissionSchemaRepository.findById(permissionSchemaId);
        if (permissionSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown permissionSchemaId[" + permissionSchemaId + "]");
        return checkPermissionSchemaAllowed(domainId, businessAccountId, permissionSchemaEntity.get());
    }

    public UUID checkPermissionSchemaAllowed(UUID domainId, UUID businessAccountId, PermissionSchemaEntity permissionSchemaEntity) throws ServiceException {
        if (permissionSchemaEntity == null)
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown permissionSchemaId");
        if (!permissionSchemaEntity.getDomainId().equals(domainId))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, permissionSchemaEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in " + permissionSchemaEntity.getDomain() + "");
        if (permissionSchemaEntity.getBusinessAccountId() != null && !permissionSchemaEntity.getBusinessAccountId().equals(businessAccountId))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, permissionSchemaEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in " + permissionSchemaEntity.getBusinessAccount() + "]");
        return permissionSchemaEntity.getId();
    }

    public UUID checkPermissionSchemaAllowed(DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        return checkPermissionSchemaAllowed(domainBusinessAccountEntity.getDomainId(), domainBusinessAccountEntity.getBusinessAccountId(), domainBusinessAccountEntity.getPermissionSchema());
    }

    public boolean hasPermission(TwinEntity twinEntity, UUID permissionId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
        return hasPermission(
                new PermissionDetectKey(
                        twinEntity.getTwinClassId(),
                        twinEntity.getPermissionSchemaSpaceId(),
                        TwinService.isAssignee(twinEntity, apiUser),
                        TwinService.isCreator(twinEntity, apiUser)),
                permissionId);
    }

    public boolean hasPermission(PermissionDetectKey permissionDetectKey, UUID permissionId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
        return twinRepository.hasPermission(
                permissionId,
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(permissionDetectKey.getPermissionSchemaSpaceId()),
                apiUser.getUser().getId(),
                TypedParameterTwins.uuidArray(apiUser.getUser().getUserGroups().getIdSetSafe()),
                TypedParameterTwins.uuidNullable(permissionDetectKey.getTwinClassId()),
                permissionDetectKey.isAssignee,
                permissionDetectKey.isCreator);
    }

    public Map<PermissionDetectKey, List<TwinEntity>> convertToDetectKeys(Collection<TwinEntity> twinEntities) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Map<PermissionDetectKey, List<TwinEntity>> detectKeys = new HashMap<>();
        PermissionDetectKey detectKey;
        for (TwinEntity twinEntity : twinEntities) {
            detectKey = new PermissionDetectKey(
                    twinEntity.getTwinClassId(),
                    twinEntity.getPermissionSchemaSpaceId(),
                    TwinService.isAssignee(twinEntity, apiUser),
                    TwinService.isCreator(twinEntity, apiUser));
            detectKeys.computeIfAbsent(detectKey, k -> new ArrayList<>());
            detectKeys.get(detectKey).add(twinEntity);
        }
        return detectKeys;
    }

    public PermissionSchemaEntity getCurrentPermissionSchema(TwinEntity twin) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        PermissionSchemaEntity permissionSchema = null;
        SpaceEntity space = null;
        if (null != twin.getPermissionSchemaSpaceId()) space = spaceRepository.findById(twin.getPermissionSchemaSpaceId()).orElse(null);
        if (null != space) permissionSchema = space.getPermissionSchema();
        if (null == permissionSchema) {
            if(apiUser.getDomain().getDomainType() == DomainType.b2b && apiUser.isBusinessAccountSpecified()) {
                final DomainBusinessAccountEntity domainBusinessAccount = domainBusinessAccountService.getDomainBusinessAccountEntitySafe(apiUser.getDomainId(), apiUser.getBusinessAccountId());
                permissionSchema = domainBusinessAccount.getPermissionSchema();
            } else {
                permissionSchema = apiUser.getDomain().getPermissionSchema();
            }
            if (null == permissionSchema) throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_SPECIFIED);
        }
        return permissionSchema;
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionEntity createPermission(PermissionEntity createEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        createEntity
                .setKey(KeyUtils.upperCaseNullSafe(createEntity.getKey(), ErrorCodeTwins.PERMISSION_KEY_INCORRECT))
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_DESCRIPTION, descriptionI18n).getId());
        validateEntityAndThrow(createEntity, EntitySmartService.EntityValidateMode.beforeSave);
        return permissionRepository.save(createEntity);
    }

    public Map<DefaultClassPermissionsPrefix, PermissionEntity> createDefaultPermissionsForNewInDomainClass(TwinClassEntity twinClassEntity) throws ServiceException {
        List<PermissionEntity> permissionsForSave = new ArrayList<>();
        PermissionGroupEntity permissionGroup = permissionGroupService.createDefaultPermissionGroupForNewInDomainClass(twinClassEntity);
        Map<DefaultClassPermissionsPrefix, PermissionEntity> newPermissions = new HashMap<>();
        for (DefaultClassPermissionsPrefix permissionPrefix : DefaultClassPermissionsPrefix.values()) {
            I18nEntity nameI18n = new I18nEntity().addTranslation(
                    new I18nTranslationEntity()
                            .setLocale(Locale.ENGLISH)
                            .setTranslation(twinClassEntity.getKey().toLowerCase().replace("_", " ") + " " + permissionPrefix.name().toLowerCase() + " permission")
            );

            I18nEntity descriptionI18n = new I18nEntity().addTranslation(
                    new I18nTranslationEntity()
                            .setLocale(Locale.ENGLISH)
                            .setTranslation(twinClassEntity.getKey().toLowerCase().replace("_", " ") + " " + permissionPrefix.name().toLowerCase() + " permission")
            );

            PermissionEntity permissionEntity = new PermissionEntity()
                    .setKey(twinClassEntity.getKey() + "_" + permissionPrefix)
                    .setPermissionGroupId(permissionGroup.getId())
                    .setPermissionGroup(permissionGroup)
                    .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_NAME, nameI18n).getId())
                    .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_DESCRIPTION, descriptionI18n).getId());
            validateEntityAndThrow(permissionEntity, EntitySmartService.EntityValidateMode.beforeSave);
            permissionsForSave.add(permissionEntity);
            newPermissions.put(permissionPrefix, permissionEntity);
        }
        entitySmartService.saveAllAndLog(permissionsForSave, permissionRepository);
        return newPermissions;
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionEntity updatePermission(PermissionEntity updateEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        PermissionEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updatePermissionKey(updateEntity, dbEntity, changesHelper);
        updatePermissionGroupId(updateEntity, dbEntity, changesHelper);
        i18nService.updateI18nFieldForEntity(nameI18n, I18nType.PERMISSION_NAME, dbEntity, PermissionEntity::getNameI18NId, PermissionEntity::setNameI18NId, PermissionEntity.Fields.nameI18NId, changesHelper);
        i18nService.updateI18nFieldForEntity(descriptionI18n, I18nType.PERMISSION_DESCRIPTION, dbEntity, PermissionEntity::getDescriptionI18NId, PermissionEntity::setDescriptionI18NId, PermissionEntity.Fields.descriptionI18NId, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    private void updatePermissionGroupId(PermissionEntity updateEntity, PermissionEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(PermissionEntity.Fields.permissionGroupId, dbEntity.getPermissionGroupId(), updateEntity.getPermissionGroupId()))
            return;
        dbEntity.setPermissionGroupId(updateEntity.getPermissionGroupId());
    }

    private void updatePermissionKey(PermissionEntity updateEntity, PermissionEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        String newKey = KeyUtils.upperCaseNullFriendly(updateEntity.getKey(), ErrorCodeTwins.PERMISSION_KEY_INCORRECT);
        if (!changesHelper.isChanged(PermissionEntity.Fields.key, dbEntity.getKey(), newKey))
            return;
        dbEntity.setKey(newKey);
    }

    /**
    * Method for checking twin ref user permissions. Only for analyse.
    * Do not use it for checking permissions.
    * You must use permissioncheck postgress routine instead (hasPermission method in this service)
    * */
    public PermissionCheckForTwinOverviewResult checkTwinAndUserForPermissions(UUID userId, UUID twinId, UUID permissionId) throws ServiceException {
        PermissionCheckForTwinOverviewResult result = new PermissionCheckForTwinOverviewResult();
        //detect permission
        TwinEntity twin = twinService.findEntitySafe(twinId);
        if (null == permissionId) {
            permissionId = twin.getViewPermissionId();
            if (null == permissionId) permissionId = twin.getTwinClass().getViewPermissionId();
            if (null == permissionId) throw new ServiceException(ErrorCodeTwins.TWIN_NOT_PROTECTED);
        }
        //permission
        PermissionEntity permission = findEntitySafe(permissionId);
        result.setPermission(permission);


        //detect permission schema


        PermissionSchemaEntity permissionSchema = getCurrentPermissionSchema(twin);
        result.setPermissionSchema(permissionSchema);


        //user permissions
        final boolean grantedForUser = permissionGrantUserRepository.existsByPermissionSchemaIdAndPermissionIdAndUserId(permissionSchema.getId(), permissionId, userId);
        result.setGrantedByUser(grantedForUser);

        //group permissions
        Kit<UserGroupEntity, UUID> groupsForUserKit = userGroupService.findGroupsForUser(userId);
        List<UserGroupEntity> grantedForGroups = new ArrayList<>();
        final List<PermissionGrantUserGroupEntity> grantedPermissions = permissionGrantUserGroupRepository.findByPermissionSchemaIdAndPermissionIdAndUserGroupIdIn(permissionSchema.getId(), permissionId, groupsForUserKit.getIdSet());
        for (PermissionGrantUserGroupEntity grantedPermission : grantedPermissions)
            grantedForGroups.add(grantedPermission.getUserGroup());
        result.setGrantedByUserGroups(new Kit<>(grantedForGroups, UserGroupEntity::getId));

        //twin roles
        result.setGrantedByTwinRoles(new HashSet<>());
        List<PermissionGrantTwinRoleEntity> permissionsSchemaTwinRoleEntities = permissionGrantTwinRoleRepository.findByPermissionSchemaIdAndPermissionIdAndTwinClassId(permissionSchema.getId(), permissionId, twin.getTwinClassId());
        TwinEntity spaceTwin = null;
        if (null != twin.getPermissionSchemaSpaceId()) {
            spaceTwin = twin.getId().equals(twin.getPermissionSchemaSpaceId()) ? twin : twinService.findEntitySafe(twin.getPermissionSchemaSpaceId());
        }
        if (!permissionsSchemaTwinRoleEntities.isEmpty()) {
            for (PermissionGrantTwinRoleEntity permissionGrantTwinRoleEntity : permissionsSchemaTwinRoleEntities) {
                if (userId.equals(twin.getAssignerUserId()) && permissionGrantTwinRoleEntity.getTwinRole().equals(TwinRole.assignee))
                    result.getGrantedByTwinRoles().add(permissionGrantTwinRoleEntity.getTwinRole());
                if (twin.getCreatedByUserId().equals(userId) && permissionGrantTwinRoleEntity.getTwinRole().equals(TwinRole.creator))
                    result.getGrantedByTwinRoles().add(permissionGrantTwinRoleEntity.getTwinRole());
                if (null != spaceTwin) {
                    if (null != spaceTwin.getAssignerUserId() && spaceTwin.getAssignerUserId().equals(userId) && permissionGrantTwinRoleEntity.getTwinRole().equals(TwinRole.space_assignee))
                        result.getGrantedByTwinRoles().add(permissionGrantTwinRoleEntity.getTwinRole());
                    if (null != spaceTwin.getCreatedByUserId() && spaceTwin.getCreatedByUserId().equals(userId) && permissionGrantTwinRoleEntity.getTwinRole().equals(TwinRole.space_creator))
                        result.getGrantedByTwinRoles().add(permissionGrantTwinRoleEntity.getTwinRole());
                }
            }
        }
        //propagation by class
        List<PermissionGrantAssigneePropagationEntity> propagations = permissionGrantAssigneePropagationRepository.findAllByPermissionSchemaIdAndPermissionId(permissionSchema.getId(), permissionId);
        List<TwinClassEntity> propagatedByTwinClasses = new ArrayList<>();
        List<TwinStatusEntity> propagatedByTwinStatuses = new ArrayList<>();
        for (var propagation : propagations) {
            if(null != propagation.getTwinClass() && twin.getTwinClassId().equals(propagation.getTwinClass().getId()))
                propagatedByTwinClasses.add(propagation.getTwinClass());
            if(null != propagation.getTwinStatus() && twin.getTwinStatusId().equals(propagation.getTwinStatus().getId()))
                propagatedByTwinStatuses.add(propagation.getTwinStatus());
        }
        result.setPropagatedByTwinClasses(new Kit<>(propagatedByTwinClasses, TwinClassEntity::getId));
        result.setPropagatedByTwinStatuses(new Kit<>(propagatedByTwinStatuses, TwinStatusEntity::getId));

        //space role user and groups
        List<SpaceRoleUserEntity> grantedSpaceRoleUsers = new ArrayList<>();
        List<SpaceRoleUserGroupEntity> grantedSpaceRoleUserGroups = new ArrayList<>();
        if (spaceTwin != null) {
            final List<SpaceRoleUserEntity> spaceRoleUsers = spaceUserRoleService.findSpaceRoleUsersByTwinIdAndUserId(spaceTwin.getId(), userId);
            final List<SpaceRoleUserGroupEntity> spaceRoleUserGroups = spaceRoleUserGroupRepository.findAllByTwinIdAndUserGroupIdIn(spaceTwin.getId(), groupsForUserKit.getIdSet());
            final List<PermissionGrantSpaceRoleEntity> grantedForSpaceRoles = permissionGrantSpaceRoleRepository.findByPermissionId(permissionId);
            for (PermissionGrantSpaceRoleEntity grantedForSpaceRole : grantedForSpaceRoles) {
                for (SpaceRoleUserEntity spaceRoleUserEntity : spaceRoleUsers) if (spaceRoleUserEntity.getSpaceRoleId().equals(grantedForSpaceRole.getSpaceRoleId())) grantedSpaceRoleUsers.add(spaceRoleUserEntity);
                for (SpaceRoleUserGroupEntity spaceRoleUserGroup : spaceRoleUserGroups) if (spaceRoleUserGroup.getSpaceRoleId().equals(grantedForSpaceRole.getSpaceRoleId())) grantedSpaceRoleUserGroups.add(spaceRoleUserGroup);
            }
        }
        result.setGrantedBySpaceRoleUsers(new Kit<>(grantedSpaceRoleUsers, SpaceRoleUserEntity::getId));
        result.setGrantedBySpaceRoleUserGroups(new Kit<>(grantedSpaceRoleUserGroups, SpaceRoleUserGroupEntity::getId));
        return result;
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class PermissionDetectKey {
        final UUID twinClassId;
        final UUID permissionSchemaSpaceId;
        final boolean isAssignee;
        final boolean isCreator;
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinClassId) {

    }

    public void checkTwinClassFieldPermission(TwinClassFieldEntity twinClassFieldEntity) {

    }

    public KitGroupedObj<PermissionEntity, UUID, UUID, PermissionGroupEntity> findPermissionsForCurrentUser() throws ServiceException {
        return findPermissionsForUser(apiUser.getUser());
    }

    public KitGroupedObj<PermissionEntity, UUID, UUID, PermissionGroupEntity> findPermissionsForUser(UUID userId) throws ServiceException {
        return findPermissionsForUser(userService.findEntitySafe(userId));
    }

    public KitGroupedObj<PermissionEntity, UUID, UUID, PermissionGroupEntity> findPermissionsForUser(UserEntity user) throws ServiceException {
        loadUserPermissions(user);
        var ret = new KitGroupedObj<>(PermissionEntity::getId, PermissionEntity::getPermissionGroupId, PermissionEntity::getPermissionGroup);
        if (CollectionUtils.isEmpty(user.getPermissions()))
            return ret;
        Kit<PermissionEntity, UUID> list = findEntitiesSafe(user.getPermissions());
        ret.addAll(list.getList());
        return ret;
    }

    private UUID detectPermissionSchemaId(ApiUser apiUser) throws ServiceException {
        UUID permissionSchemaId;
        if (apiUser.isBusinessAccountSpecified() && apiUser.getBusinessAccount() != null) {
            DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountService.getDomainBusinessAccountEntitySafe(apiUser.getDomain().getId(), apiUser.getBusinessAccount().getId());
            checkPermissionSchemaAllowed(domainBusinessAccountEntity);
            permissionSchemaId = domainBusinessAccountEntity.getPermissionSchemaId();
        } else {
            permissionSchemaId = apiUser.getDomain().getPermissionSchemaId();
        }
        return permissionSchemaId;
    }

    public void checkUserIsCurrentOrHasPermission(UUID userId, boolean anyOf, Permissions... permissions) throws ServiceException {
        if (userId.equals(authService.getApiUser().getUserId())) {
            return;
        }
        Set<UUID> permissionIds = Arrays.stream(permissions)
                .map(Permissions::getId)
                .collect(Collectors.toSet());
        if (!currentUserHasPermission(anyOf, permissionIds)) {
            throw new ServiceException(ErrorCodeTwins.NO_REQUIRED_PERMISSION, "User does not have required permissions");
        }
    }

    public boolean currentUserHasPermission(UUID permissionId) throws ServiceException {
       return currentUserHasPermission(false, Set.of(permissionId));
    }

    public boolean currentUserHasPermission(Permissions permission) throws ServiceException {
        return currentUserHasPermission(false, Set.of(permission.getId()));
    }

    public boolean currentUserHasPermission(boolean anyOf, Set<UUID> permissions) throws ServiceException {
        if (CollectionUtils.isEmpty(permissions))
            return false;

        ApiUser apiUser = authService.getApiUser();
        if (!apiUser.isUserSpecified())
            return false;

        loadUserPermissions(apiUser.getUser());
        return anyOf
            ? apiUser.getUser().getPermissions().stream().anyMatch(permissions::contains)
            : apiUser.getUser().getPermissions().containsAll(permissions);
    }

    public void loadCurrentUserPermissions() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!apiUser.isUserSpecified())
            return;
        loadUserPermissions(apiUser.getUser());
    }

    public void loadUserPermissions(UserEntity user) throws ServiceException {
        if (user.getPermissions() != null)
            return;
        ApiUser apiUser = authService.getApiUser();
        // we check it here and not in loadCurrentUserPermissions method for more safety
        if (user.getId().equals(apiUser.getUserId()) && apiUser.getActAsUserStep() == ApiUser.ActAsUserStep.PERMISSION_CHECK_NEEDED) {
            loadUserPermissions(apiUser.getMachineUser()); // self-call, be careful
            if (!apiUser.getMachineUser().getPermissions().contains(Permissions.ACT_AS_USER.getId())) {
                throw new ServiceException(ErrorCodeTwins.NO_REQUIRED_PERMISSION, "Current user has not ACT_AS_USER permission");
            }
            apiUser.setActAsUserStep(ApiUser.ActAsUserStep.USER_GROUP_INVOLVE_NEEDED);
        }
        UUID permissionSchemaId = detectPermissionSchemaId(apiUser);
        userGroupService.loadGroups(user);
        Set<UUID> userGroupIds = user.getUserGroups().getIdSetSafe();
        List<UUID> permissionList = permissionGrantUserRepository.findAllPermissionsForUser(permissionSchemaId, user.getId(), collectionUuidsToSqlArray(userGroupIds));
        user.setPermissions(new HashSet<>(permissionList));
    }


    public void forceDeleteSchemas(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> schemasToDelete = permissionSchemaRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(schemasToDelete, permissionSchemaRepository);
    }

    public enum DefaultClassPermissionsPrefix {
        VIEW, EDIT, CREATE, DELETE
    }

}

