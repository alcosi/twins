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
import org.cambium.common.util.StreamUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.permission.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
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
    final TwinRepository twinRepository;
    @Lazy
    final AuthService authService;
    @Lazy
    final DomainService domainService;
    final UserGroupService userGroupService;
    @Lazy
    final EntitySmartService entitySmartService;

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
        return true;
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

