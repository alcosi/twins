package org.twins.core.service.permission;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.StreamUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.permission.*;
import org.twins.core.dao.space.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DomainResolver;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.dto.rest.permission.TwinRole;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.space.SpaceRoleService;
import org.twins.core.service.space.SpaceUserRoleService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserGroupService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService extends EntitySecureFindServiceImpl<PermissionEntity> {

    final PermissionRepository permissionRepository;
    final PermissionSchemaRepository permissionSchemaRepository;
    final PermissionSchemaUserRepository permissionSchemaUserRepository;
    final PermissionSchemaUserGroupRepository permissionSchemaUserGroupRepository;
    final PermissionSchemaTwinRoleRepository permissionSchemaTwinRoleRepository;
    final PermissionSchemaSpaceRolesRepository permissionSchemaSpaceRolesRepository;
    final SpaceRepository spaceRepository;
    final SpaceRoleService spaceRoleService;
    final SpaceUserRoleService spaceUserRoleService;
    final SpaceRoleUserGroupRepository spaceRoleUserGroupRepository;

    final TwinRepository twinRepository;
    @Lazy
    final TwinService twinService;
    @Lazy
    final AuthService authService;
    @Lazy
    final DomainService domainService;
    final UserGroupService userGroupService;
    @Lazy
    final EntitySmartService entitySmartService;
    final PermissionGroupService permissionGroupService;

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
        userGroupService.loadGroups(apiUser);
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
        userGroupService.loadGroups(apiUser);
        return twinRepository.hasPermission(
                permissionId,
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(permissionDetectKey.getPermissionSchemaSpaceId()),
                apiUser.getUser().getId(),
                TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
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

    @Override
    public CrudRepository<PermissionEntity, UUID> entityRepository() {
        return permissionRepository;
    }

    @Override
    public boolean isEntityReadDenied(PermissionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(PermissionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getKey() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty key");
        if (entity.getPermissionGroupId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionGroupId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionGroup() == null)
                    entity.setPermissionGroup(permissionGroupService.findEntitySafe(entity.getPermissionGroupId()));
            case afterRead:
                return permissionGroupService.validateEntity(entity.getPermissionGroup(), EntitySmartService.EntityValidateMode.afterRead);
        }
        return true;
    }

    public PermissionSchemaEntity loadSchemaForDomain(DomainEntity domain) {
        if(null != domain.getPermissionSchema())
            return domain.getPermissionSchema();
        final PermissionSchemaEntity permissionSchema = permissionSchemaRepository.findById(domain.getPermissionSchemaId()).orElse(null);
        domain.setPermissionSchema(permissionSchema);
        return permissionSchema;
    }

    public PermissionSchemaEntity getCurrentPermissionSchema(TwinEntity twin) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        PermissionSchemaEntity permissionSchema = null;
        SpaceEntity space = null;
        if (null != twin.getPermissionSchemaSpaceId()) space = spaceRepository.findById(twin.getPermissionSchemaSpaceId()).orElse(null);
        if (null != space) permissionSchema = space.getPermissionSchema();
        if (null == permissionSchema) {
            if(apiUser.isBusinessAccountSpecified()) {
                final DomainBusinessAccountEntity domainBusinessAccount = domainService.getDomainBusinessAccountEntitySafe(apiUser.getDomainId(), apiUser.getBusinessAccountId());
                permissionSchema = domainBusinessAccount.getPermissionSchema();
            } else {
                permissionSchema = loadSchemaForDomain(apiUser.getDomain());
            }
            if (null == permissionSchema) throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_SPECIFIED);
        }
        return permissionSchema;
    }


    /**
    * Method for checking twin ref user permissions. Only for analyse.
    * Do not use it for checking permissions.
    * You must use permissioncheck postgress routine instead
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
        final boolean grantedForUser = permissionSchemaUserRepository.existsByPermissionSchemaIdAndPermissionIdAndUserId(permissionSchema.getId(), permissionId, userId);
        result.setGrantedByUser(grantedForUser);

        //group permissions
        Kit<UserGroupEntity, UUID> groupsForUserKit = new Kit<>(userGroupService.findGroupsForUser(userId), UserGroupEntity::getId);
        List<UserGroupEntity> grantedForGroups = new ArrayList<>();
        final List<PermissionSchemaUserGroupEntity> grantedPermissions = permissionSchemaUserGroupRepository.findByPermissionSchemaIdAndPermissionIdAndUserGroupIdIn(permissionSchema.getId(), permissionId, groupsForUserKit.getIdSet());
        for (PermissionSchemaUserGroupEntity grantedPermission : grantedPermissions)
            grantedForGroups.add(grantedPermission.getUserGroup());
        result.setGrantedByUserGroups(new Kit<>(grantedForGroups, UserGroupEntity::getId));

        //twin roles
        result.setGrantedByTwinRoles(new HashSet<>());
        List<PermissionSchemaTwinRoleEntity> permissionsSchemaTwinRoleEntities = permissionSchemaTwinRoleRepository.findByPermissionSchemaIdAndPermissionIdAndTwinClassId(permissionSchema.getId(), permissionId, twin.getTwinClassId());
        TwinEntity spaceTwin = null;
        if (null != twin.getPermissionSchemaSpaceId()) {
            spaceTwin = twin.getId().equals(twin.getPermissionSchemaSpaceId()) ? twin : twinService.findEntitySafe(twin.getPermissionSchemaSpaceId());
        }
        if (!permissionsSchemaTwinRoleEntities.isEmpty()) {
            for (PermissionSchemaTwinRoleEntity permissionSchemaTwinRoleEntity : permissionsSchemaTwinRoleEntities) {
                if (twin.getAssignerUserId().equals(userId) && permissionSchemaTwinRoleEntity.getTwinRole().equals(TwinRole.assignee))
                    result.getGrantedByTwinRoles().add(permissionSchemaTwinRoleEntity.getTwinRole());
                if (twin.getCreatedByUserId().equals(userId) && permissionSchemaTwinRoleEntity.getTwinRole().equals(TwinRole.creator))
                    result.getGrantedByTwinRoles().add(permissionSchemaTwinRoleEntity.getTwinRole());
                if (null != spaceTwin) {
                    if (null != spaceTwin.getAssignerUserId() && spaceTwin.getAssignerUserId().equals(userId) && permissionSchemaTwinRoleEntity.getTwinRole().equals(TwinRole.space_assignee))
                        result.getGrantedByTwinRoles().add(permissionSchemaTwinRoleEntity.getTwinRole());
                    if (null != spaceTwin.getCreatedByUserId() && spaceTwin.getCreatedByUserId().equals(userId) && permissionSchemaTwinRoleEntity.getTwinRole().equals(TwinRole.space_creator))
                        result.getGrantedByTwinRoles().add(permissionSchemaTwinRoleEntity.getTwinRole());
                }
            }
        }

        //space role user and groups
        List<SpaceRoleUserEntity> grantedSpaceRoleUsers = new ArrayList<>();
        List<SpaceRoleUserGroupEntity> grantedSpaceRoleUserGroups = new ArrayList<>();
        if (spaceTwin != null) {
            final List<SpaceRoleUserEntity> spaceRoleUsers = spaceUserRoleService.findSpaceRoleUsersByTwinIdAndUserId(spaceTwin.getId(), userId);
            final List<SpaceRoleUserGroupEntity> spaceRoleUserGroups = spaceRoleUserGroupRepository.findAllByTwinIdAndUserGroupIdIn(spaceTwin.getId(), groupsForUserKit.getIdSet());
            final List<PermissionSchemaSpaceRolesEntity> grantedForSpaceRoles = permissionSchemaSpaceRolesRepository.findByPermissionId(permissionId);
            for (PermissionSchemaSpaceRolesEntity grantedForSpaceRole : grantedForSpaceRoles) {
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

    public FindUserPermissionsResult findPermissionsForUser(UUID userId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomain().getId();
        UUID permissionSchemaId = detectPermissionSchemaId(apiUser);
        return new FindUserPermissionsResult()
                .setPermissionsByUser(permissionSchemaUserRepository.findByPermissionSchemaIdAndUserId(
                                permissionSchemaId, userId)
                        .stream().filter(p -> StreamUtils.andLogFilteredOutValues(p.getPermission().getPermissionGroup().getDomainId().equals(domainId), p.getPermission().easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for domain[" + domainId + "]")).toList()) // filter bad configured permissions
                .setPermissionByUserGroup(permissionSchemaUserGroupRepository.findByPermissionSchemaIdAndUserGroupIdIn(
                                permissionSchemaId,
                                userGroupService.findGroupsForUser(userId).stream().map(UserGroupEntity::getId).collect(Collectors.toList()))
                        .stream().filter(p -> StreamUtils.andLogFilteredOutValues(p.getPermission().getPermissionGroup().getDomainId().equals(domainId), p.getPermission().easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for domain[" + domainId + "]")).toList()); // filter bad configured permissions;
    }

    private UUID detectPermissionSchemaId(ApiUser apiUser) throws ServiceException {
        UUID permissionSchemaId;
        if (apiUser.getBusinessAccount() != null) {
            DomainBusinessAccountEntity domainBusinessAccountEntity = domainService.getDomainBusinessAccountEntitySafe(apiUser.getDomain().getId(), apiUser.getBusinessAccount().getId());
            checkPermissionSchemaAllowed(domainBusinessAccountEntity);
            permissionSchemaId = domainBusinessAccountEntity.getPermissionSchemaId();
        } else {
            permissionSchemaId = apiUser.getDomain().getPermissionSchemaId();
        }
        return permissionSchemaId;
    }

    public boolean currentUserHasPermission(UUID permissionId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        loadUserPermissions(apiUser);
        return apiUser.getPermissions().contains(permissionId);
    }

    public void loadUserPermissions(ApiUser apiUser) throws ServiceException {
        if (apiUser.getPermissions() != null)
            return;
        UUID permissionSchemaId = detectPermissionSchemaId(apiUser);
        userGroupService.loadGroups(apiUser);
        List<UUID> permissionList = permissionSchemaUserRepository
                .findPermissionIdByPermissionSchemaIdAndUserId(permissionSchemaId, apiUser.getUser().getId());
        Set<UUID> permissionSet = new HashSet<>(permissionList);
        permissionList = permissionSchemaUserGroupRepository
                .findPermissionIdByPermissionSchemaIdAndUserGroupIdIn(permissionSchemaId, apiUser.getUserGroups());
        permissionSet.addAll(permissionList);
        apiUser.setPermissions(permissionSet);
    }

    @Data
    @Accessors(chain = true)
    public static class FindUserPermissionsResult {
        private UUID userId;
        private List<PermissionSchemaUserEntity> permissionsByUser;
        private List<PermissionSchemaUserGroupEntity> permissionByUserGroup;

        public List<PermissionEntity> collectPermissions() {
            List<PermissionEntity> ret = new ArrayList<>();
            if (permissionsByUser != null)
                ret.addAll(permissionsByUser.stream().map(PermissionSchemaUserEntity::getPermission).toList());
            if (permissionByUserGroup != null)
                ret.addAll(permissionByUserGroup.stream().map(PermissionSchemaUserGroupEntity::getPermission).toList());
            return ret.stream().filter(StreamUtils.distinctByKey(PermissionEntity::getId)).toList();
        }

        public List<ImmutablePair<PermissionGroupEntity, List<PermissionEntity>>> collectPermissionGroups() {
            List<PermissionEntity> distinctPermissions = collectPermissions();
            List<ImmutablePair<PermissionGroupEntity, List<PermissionEntity>>> ret = new ArrayList<>();
            Map<UUID, List<PermissionEntity>> mapGrouped = distinctPermissions.stream().collect(Collectors.groupingBy(PermissionEntity::getPermissionGroupId));
            for (Map.Entry<UUID, List<PermissionEntity>> entry : mapGrouped.entrySet()) {
                ret.add(new ImmutablePair<>(entry.getValue().get(0).getPermissionGroup(), entry.getValue()));
            }
            return ret;
        }
    }

    public void forceDeleteSchemas(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> schemasToDelete = permissionSchemaRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(schemasToDelete, permissionSchemaRepository);
    }

}

