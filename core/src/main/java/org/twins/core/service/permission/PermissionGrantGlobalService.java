package org.twins.core.service.permission;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionGrantGlobalEntity;
import org.twins.core.dao.permission.PermissionGrantGlobalRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.user.UserService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionGrantGlobalService extends TwinsEntitySecureFindService<PermissionGrantGlobalEntity> {
    @Getter
    private final PermissionGrantGlobalRepository repository;
    @Lazy
    private final PermissionService permissionService;
    @Lazy
    private final UserGroupService userGroupService;
    @Lazy
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantGlobalEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantGlobalEntity, UUID> entityGetIdFunction() {
        return PermissionGrantGlobalEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantGlobalEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(PermissionGrantGlobalEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadPermission(PermissionGrantGlobalEntity src) throws ServiceException {
        loadPermission(Collections.singletonList(src));
    }

    public void loadPermission(Collection<PermissionGrantGlobalEntity> srcCollection) throws ServiceException {
        permissionService.load(srcCollection,
                PermissionGrantGlobalEntity::getPermissionId,
                PermissionGrantGlobalEntity::getPermission,
                PermissionGrantGlobalEntity::setPermission);
    }

    public void loadUserGroup(PermissionGrantGlobalEntity src) throws ServiceException {
        loadUserGroup(Collections.singletonList(src));
    }

    public void loadUserGroup(Collection<PermissionGrantGlobalEntity> srcCollection) throws ServiceException {
        userGroupService.load(srcCollection,
                PermissionGrantGlobalEntity::getUserGroupId,
                PermissionGrantGlobalEntity::getUserGroup,
                PermissionGrantGlobalEntity::setUserGroup);
    }

    public void loadGrantedByUser(PermissionGrantGlobalEntity src) throws ServiceException {
        loadGrantedByUser(Collections.singletonList(src));
    }

    public void loadGrantedByUser(Collection<PermissionGrantGlobalEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                PermissionGrantGlobalEntity::getGrantedByUserId,
                PermissionGrantGlobalEntity::getGrantedByUser,
                PermissionGrantGlobalEntity::setGrantedByUser);
    }
}
