package org.twins.core.service;

import org.cambium.common.exception.ServiceException;

import java.util.UUID;

public interface EntitySecureFindService<T> {
    UUID checkId(UUID id, EntitySmartService.CheckMode checkMode) throws ServiceException;
    T findEntity(UUID entityId, EntitySmartService.FindMode findMode,  EntitySmartService.ReadPermissionCheckMode permissionCheckMode) throws ServiceException;
    boolean isEntityReadDenied(T entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException;
}
