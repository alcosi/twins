package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

@Slf4j
public abstract class EntitySecureFindServiceImpl<T> implements EntitySecureFindService<T> {
    @Autowired
    EntitySmartService entitySmartService;

    @Override
    public UUID checkId(UUID id, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(id, entityName(), entityRepository(), checkMode);
    }

    public abstract String entityName();
    public abstract CrudRepository<T, UUID> entityRepository();

    @Override
    public T findEntity(UUID entityId, EntitySmartService.FindMode findMode, EntitySmartService.ReadPermissionCheckMode permissionCheckMode) throws ServiceException {
        T entity = entitySmartService.findById(entityId, entityName(), entityRepository(), findMode);
        if (entity == null || permissionCheckMode.equals(EntitySmartService.ReadPermissionCheckMode.none))
            return entity;
        if (isEntityReadDenied(entity, permissionCheckMode))
            return null;
        return entity;
    }

    public T findEntitySafe(UUID entityId) throws ServiceException {
        return findEntity(entityId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
    }

    public boolean isEntityReadDenied(T entity) {
        try {
            return isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog);
        } catch (ServiceException e) {
            log.warn("Incorrect method call", e);
            return true;
        }
    }

    @Override
    public abstract boolean isEntityReadDenied(T entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException;
}
