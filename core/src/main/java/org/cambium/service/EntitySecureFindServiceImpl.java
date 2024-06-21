package org.cambium.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

@Slf4j
public abstract class EntitySecureFindServiceImpl<T> implements EntitySecureFindService<T> {
    @Autowired
    public EntitySmartService entitySmartService;

    @Override
    public UUID checkId(UUID id, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(id, entityRepository(), checkMode);
    }

    public abstract CrudRepository<T, UUID> entityRepository();

    public T findEntity(UUID entityId,
                        EntitySmartService.FindMode findMode,
                        EntitySmartService.ReadPermissionCheckMode permissionCheckMode) throws ServiceException {
        return findEntity(entityId, findMode, permissionCheckMode, EntitySmartService.EntityValidateMode.afterRead);
    }

    @Override
    public T findEntity(UUID entityId,
                        EntitySmartService.FindMode findMode,
                        EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                        EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        T entity = entitySmartService.findById(entityId, entityRepository(), findMode);
        if (entity == null || permissionCheckMode.equals(EntitySmartService.ReadPermissionCheckMode.none))
            return entity;
        if (isEntityReadDenied(entity, permissionCheckMode))
            return null;
        validateEntityAndThrow(entity, entityValidateMode);
        return entity;
    }

    public String getValidationErrorMessage(T entity) throws ServiceException {
        if (entity instanceof EasyLoggable easyLoggable)
            return easyLoggable.easyLog(EasyLoggable.Level.NORMAL) + " is invalid. Please check log for details";
        else
            return "entity of class[" + entity.getClass().getSimpleName() + "] is invalid";
    }

    public T findEntitySafe(UUID entityId) throws ServiceException {
        return findEntity(entityId,
                EntitySmartService.FindMode.ifEmptyThrows,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows,
                EntitySmartService.EntityValidateMode.afterRead);
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

    public boolean validateEntityAndLog(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entityValidateMode == EntitySmartService.EntityValidateMode.none)
            return true;
        if (!validateEntity(entity, entityValidateMode)) {
            log.error(getValidationErrorMessage(entity));
            return false;
        }
        return true;
    }

    public boolean validateEntityAndThrow(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entityValidateMode == EntitySmartService.EntityValidateMode.none)
            return true;
        if (!validateEntity(entity, entityValidateMode)) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, getValidationErrorMessage(entity));
        }
        return true;
    }

    @Override
    public abstract boolean validateEntity(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;

    public boolean logErrorAndReturnFalse(String message) {
        log.error(message);
        return false;
    }
}
