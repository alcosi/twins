package org.cambium.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public abstract class EntitySecureFindServiceImpl<T> implements EntitySecureFindService<T> {
    @Autowired
    public EntitySmartService entitySmartService;

    @Override
    public UUID checkId(UUID id, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(id, entityRepository(), checkMode);
    }

    public abstract CrudRepository<T, UUID> entityRepository();
    public abstract Function<T, UUID> entityGetIdFunction();

    public Optional<T> findByKey(String key) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "Method findByKey is not implemented in service");
    }

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
        return checkResult(permissionCheckMode, entityValidateMode, entity);
    }

    @Override
    public T findEntity(String entityKey,
                        EntitySmartService.FindMode findMode,
                        EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                        EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        T entity = entitySmartService.checkOptional(findByKey(entityKey), entityKey, entityRepository(), findMode);
        return checkResult(permissionCheckMode, entityValidateMode, entity);
    }

    private T checkResult(EntitySmartService.ReadPermissionCheckMode permissionCheckMode, EntitySmartService.EntityValidateMode entityValidateMode, T entity) throws ServiceException {
        if (entity == null || permissionCheckMode.equals(EntitySmartService.ReadPermissionCheckMode.none))
            return entity;
        if (isEntityReadDenied(entity, permissionCheckMode))
            return null;
        validateEntityAndThrow(entity, entityValidateMode);
        return entity;
    }

    @Override
    public Kit<T, UUID> findEntities(Collection<UUID> entityIds,
                                EntitySmartService.ListFindMode findMode,
                                EntitySmartService.ReadPermissionCheckMode permissionCheckMode,
                                EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        Kit<T, UUID> kit = entitySmartService.findByIdIn(entityIds, entityRepository(), entityGetIdFunction(), findMode);
        Iterator<T> iterator = kit.iterator();
        while (iterator.hasNext()) {
            T entity = iterator.next();
            if (entity == null || permissionCheckMode.equals(EntitySmartService.ReadPermissionCheckMode.none))
                continue;
            if (isEntityReadDenied(entity, permissionCheckMode))
                iterator.remove();
            validateEntityAndThrow(entity, entityValidateMode);
        }
        return kit;
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

    public T findEntitySafe(String entityKey) throws ServiceException {
        return findEntity(entityKey,
                EntitySmartService.FindMode.ifEmptyThrows,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows,
                EntitySmartService.EntityValidateMode.afterRead);
    }

    public Kit<T, UUID> findEntitiesSafe(Collection<UUID> entityIds) throws ServiceException {
        return findEntities(entityIds,
                EntitySmartService.ListFindMode.ifMissedThrows,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows,
                EntitySmartService.EntityValidateMode.afterRead);
    }

    public T checkEntityReadAllow(T entity) throws ServiceException {
        isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        return entity;
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

    public T saveSafe(T entity) throws ServiceException {
        validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
        return entityRepository().save(entity);
    }

    public T updateSafe(T entity, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.hasChanges()) {
            validateEntity(entity, EntitySmartService.EntityValidateMode.beforeSave);
            return entitySmartService.saveAndLogChanges(entity, entityRepository(), changesHelper);
        }
        return entity;
    }

    public void deleteSafe(UUID id) throws ServiceException {
        findEntitySafe(id);
        entitySmartService.deleteAndLog(id, entityRepository());
    }

    public T validateEntityAndThrow(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entityValidateMode == EntitySmartService.EntityValidateMode.none)
            return entity;
        if (!validateEntity(entity, entityValidateMode)) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, getValidationErrorMessage(entity));
        }
        return entity;
    }

    @Override
    public abstract boolean validateEntity(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;

    public boolean logErrorAndReturnFalse(String message) {
        log.error(message);
        return false;
    }

    public boolean logErrorAndReturnTrue(String message) {
        log.error(message);
        return true;
    }

    protected <T, R> void updateEntityField(T updateEntity, T dbEntity, Function<T, R> getFunction, BiConsumer<T, R> setFunction, String field, ChangesHelper changesHelper) {
        //todo if the new field will have a nullify marker, then we will set the field to null,
        // which will throw an error when saving if the entity field should not be null
        R updateValue = getFunction.apply(updateEntity);
        R dbValue = getFunction.apply(dbEntity);
        if (!changesHelper.isChanged(field, dbValue, updateValue))
            return;
        setFunction.accept(dbEntity, updateValue);
    }
}
