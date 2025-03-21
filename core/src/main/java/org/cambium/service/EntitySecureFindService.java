package org.cambium.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;

import java.util.Collection;
import java.util.UUID;

public interface EntitySecureFindService<T> {
    UUID checkId(UUID id, EntitySmartService.CheckMode checkMode) throws ServiceException;
    T findEntity(UUID entityId,
                 EntitySmartService.FindMode findMode,
                 EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                 EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
    T findEntity(String key,
                 EntitySmartService.FindMode findMode,
                 EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                 EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
    Kit<T, UUID> findEntities(Collection<UUID> entityId,
                              EntitySmartService.ListFindMode findMode,
                              EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                              EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
    boolean isEntityReadDenied(T entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException;
    boolean validateEntity(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;
}
