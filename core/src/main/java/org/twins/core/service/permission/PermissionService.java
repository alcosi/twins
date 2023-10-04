package org.twins.core.service.permission;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.permission.*;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.user.UserGroupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    final PermissionRepository permissionRepository;
    final PermissionSchemaRepository permissionSchemaRepository;
    final PermissionSchemaUserRepository permissionSchemaUserRepository;
    final PermissionSchemaUserGroupRepository permissionSchemaUserGroupRepository;
    @Lazy
    final AuthService authService;
    @Lazy
    final DomainService domainService;
    final UserGroupService userGroupService;

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
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, permissionSchemaEntity.logShort() + " is not allowed in " + permissionSchemaEntity.getDomain() + "");
        if (permissionSchemaEntity.getBusinessAccountId() != null && !permissionSchemaEntity.getBusinessAccountId().equals(businessAccountId))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, permissionSchemaEntity.logShort() + " is not allowed in " + permissionSchemaEntity.getBusinessAccount() + "]");
        return permissionSchemaEntity.getId();
    }

    public UUID checkPermissionSchemaAllowed(DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        return checkPermissionSchemaAllowed(domainBusinessAccountEntity.getDomainId(), domainBusinessAccountEntity.getBusinessAccountId(), domainBusinessAccountEntity.getPermissionSchema());
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinClassId) {

    }

    public void checkTwinClassFieldPermission(TwinClassFieldEntity twinClassFieldEntity) {

    }

    public FindUserPermissionsResult findPermissionsForUser(UUID userId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainService.getDomainBusinessAccountEntitySafe(apiUser.getDomain().getId(), apiUser.getBusinessAccount().getId());
        checkPermissionSchemaAllowed(domainBusinessAccountEntity);
        PermissionSchemaEntity permissionSchemaEntity = domainBusinessAccountEntity.getPermissionSchema();
        return new FindUserPermissionsResult()
                .setPermissionsByUser(permissionSchemaUserRepository.findByPermissionSchemaIdAndUserId(permissionSchemaEntity.getId(), userId))
                .setPermissionByUserGroup(permissionSchemaUserGroupRepository.findByPermissionSchemaIdAndUserGroupIdIn(
                        permissionSchemaEntity.getId(),
                        userGroupService.findGroupsForUser(userId).stream().map(UserGroupEntity::getId).collect(Collectors.toList())));
    }

    @Data
    @Accessors(chain = true)
    public static class FindUserPermissionsResult {
        private UUID userId;
        private List<PermissionSchemaUserEntity> permissionsByUser;
        private List<PermissionSchemaUserGroupEntity> permissionByUserGroup;

        public List<PermissionEntity> collect() {
            List<PermissionEntity> ret = new ArrayList<>();
            if (permissionsByUser != null)
                ret.addAll(permissionsByUser.stream().map(PermissionSchemaUserEntity::getPermission).toList());
            if (permissionByUserGroup != null)
                ret.addAll(permissionByUserGroup.stream().map(PermissionSchemaUserGroupEntity::getPermission).toList());
            return ret;
        }
    }

}

