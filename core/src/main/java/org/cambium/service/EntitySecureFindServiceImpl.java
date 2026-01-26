package org.cambium.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.twins.core.exception.ErrorCodeTwins;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public abstract class EntitySecureFindServiceImpl<T> implements EntitySecureFindService<T> {
    @Autowired
    public EntitySmartService entitySmartService;

    @Autowired
    private CacheManager cacheManager;

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
        if (entity == null)
            return null;
        switch (permissionCheckMode) {
            case none:
                break;
            case ifDeniedThrows:
                if (isEntityReadDenied(entity, permissionCheckMode)) {
                    //normally detailed exception must be thrown from isEntityReadDenied method
                    EntitySmartService.entityReadDenied(permissionCheckMode, "read is denied for " + entity);
                }
                break;
            case ifDeniedLog:
                if (isEntityReadDenied(entity, permissionCheckMode)) {
                    //logging must be implemented in isEntityReadDenied method
                    return null;
                }
                break;
        }
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
            return easyLoggable.logNormal() + " is invalid. Please check log for details";
        else
            return "entity of class[" + entity.getClass().getSimpleName() + "] is invalid";
    }

    public T findEntitySafeUncached(UUID entityId) throws ServiceException {
        return findEntity(entityId,
                EntitySmartService.FindMode.ifEmptyThrows,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows,
                EntitySmartService.EntityValidateMode.afterRead);
    }

    public T findEntityPublic(UUID entityId) throws ServiceException {
        return findEntity(entityId,
                EntitySmartService.FindMode.ifEmptyThrows,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.afterRead);
    }

    @SuppressWarnings("unchecked")
    public T findEntitySafe(UUID entityId) throws ServiceException {
        if (entityId == null)
            throw new ServiceException(ErrorCodeTwins.UUID_IS_NULL, "no " + entitySmartService.entityShortName(entityRepository()) + " can be found by null id");

        T entity = null;
        switch (getCacheSupportType()) {
            case GLOBAL -> {
                Class<T> clazz = getEntityClass();
                String cacheKey = clazz.getSimpleName();
                Cache cache = cacheManager.getCache(cacheKey);
                if (cache != null) {
                    entity = cache.get(entityId, clazz);
                    if (entity == null) {
                        entity = findEntitySafeUncached(entityId);
                        if (entity != null) cache.put(entityId, entity);
                    }
                }
            }
            case REQUEST -> {
                Class<T> clazz = getEntityClass();
                String cacheKey = clazz.getSimpleName();
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    String requestCacheKey = cacheKey + "_" + entityId;
                    entity = (T) requestAttributes.getAttribute(requestCacheKey, RequestAttributes.SCOPE_REQUEST);
                    if (entity == null) {
                        entity = findEntitySafeUncached(entityId);
                        if (entity != null)
                            requestAttributes.setAttribute(requestCacheKey, entity, RequestAttributes.SCOPE_REQUEST);
                    }
                } else {
                    //todo use thread local
                    entity = findEntitySafeUncached(entityId);
                }
            }
            case NONE -> entity = findEntitySafeUncached(entityId);

        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getEntityClass() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                return (Class<T>) actualTypeArguments[0];
            }
        }
        throw new IllegalStateException("Cannot determine entity class");
    }

    public static enum CacheSupportType {
        GLOBAL, REQUEST, NONE
    }

    public CacheSupportType getCacheSupportType() {
        return CacheSupportType.NONE;
    }


    public T findEntitySafe(String entityKey) throws ServiceException {
        return findEntity(entityKey,
                EntitySmartService.FindMode.ifEmptyThrows,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows,
                EntitySmartService.EntityValidateMode.afterRead);
    }

    public T findEntityPublic(String entityKey) throws ServiceException {
        return findEntity(entityKey,
                EntitySmartService.FindMode.ifEmptyThrows,
                EntitySmartService.ReadPermissionCheckMode.none,
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

    @Override
    public void beforeValidateEntities(Collection<T> entities, EntitySmartService.EntityValidateMode entityValidateMode) {

    }

    public T saveSafe(T entity) throws ServiceException {
        validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
        return entityRepository().save(entity);
    }

    public Iterable<T> saveSafe(Collection<T> entities) throws ServiceException {
        validateEntitiesAndThrow(entities, EntitySmartService.EntityValidateMode.beforeSave);
        return entitySmartService.saveAllAndLog(entities, entityRepository());
    }

    public T updateSafe(T entity, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.hasChanges()) {
            validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
            return entitySmartService.saveAndLogChanges(entity, entityRepository(), changesHelper);
        }
        return entity;
    }

    public Iterable<T> updateSafe(ChangesHelperMulti<T> changesHelperMulti) throws ServiceException {
        List<T> entityList = new ArrayList<>();
        StringBuilder changes = new StringBuilder();
        for (var entry : changesHelperMulti.entrySet()) {
            if (entry.getValue().hasChanges()) {
                validateEntityAndThrow(entry.getKey(), EntitySmartService.EntityValidateMode.beforeSave);
                entityList.add(entry.getKey());
                changes.append(entry.getValue().collectForLog());
            }
        }
        if (CollectionUtils.isNotEmpty(entityList))
            return entitySmartService.saveAllAndLogChanges(entityList, entityRepository(), changes);
        else
            return Collections.emptyList();
    }

    public void deleteSafe(UUID id) throws ServiceException {
        findEntitySafe(id);
        entitySmartService.deleteAndLog(id, entityRepository());
    }

    public void deleteSafe(Collection<UUID> ids) throws ServiceException {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        Kit<T, UUID> entities = findEntitiesSafe(ids);

        if (entities.isNotEmpty()) {
            entitySmartService.deleteAllAndLog(entities.getIdSet(), entityRepository());
        }
    }

    public T validateEntityAndThrow(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entityValidateMode == EntitySmartService.EntityValidateMode.none)
            return entity;
        if (!validateEntity(entity, entityValidateMode)) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, getValidationErrorMessage(entity));
        }
        return entity;
    }

    public Collection<T> validateEntitiesAndThrow(Collection<T> entities, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entityValidateMode == EntitySmartService.EntityValidateMode.none) {
            return entities;
        }

        if (!validateEntities(entities, entityValidateMode)) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Entity from list is invalid");
        }

        return entities;
    }

    @Override
    public abstract boolean validateEntity(T entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException;

    // todo somehow force other devs to implement this method if they want to use validateEntitiesAndThrow
    public boolean validateEntities(Collection<T> entities, EntitySmartService.EntityValidateMode entityValidateMode) {
        return true;
    }

    public boolean logErrorAndReturnFalse(String message) {
        log.error(message);
        return false;
    }

    public boolean logErrorAndReturnTrue(String message) {
        log.error(message);
        return true;
    }

    protected <T, R> void updateEntityFieldByEntity(T updateEntity, T dbEntity, Function<T, R> getFunction, BiConsumer<T, R> setFunction, String field, ChangesHelper changesHelper) {
        //todo if the new field will have a nullify marker, then we will set the field to null,
        // which will throw an error when saving if the entity field should not be null
        R updateValue = getFunction.apply(updateEntity);
        R dbValue = getFunction.apply(dbEntity);
        if (!changesHelper.isChanged(field, dbValue, updateValue))
            return;
        if (updateValue instanceof UUID && UuidUtils.NULLIFY_MARKER.equals(updateValue))
            updateValue = null;
        setFunction.accept(dbEntity, updateValue);
    }

    protected <T, R> void updateEntityFieldByValue(Object updateValue, T dbEntity, Function<T, R> getFunction, BiConsumer<T, R> setFunction, String field, ChangesHelper changesHelper) {
        //todo if the new field will have a nullify marker, then we will set the field to null,
        // which will throw an error when saving if the entity field should not be null
        R dbValue = getFunction.apply(dbEntity);
        if (!changesHelper.isChanged(field, dbValue, updateValue))
            return;
        setFunction.accept(dbEntity, (R) updateValue);
    }

    protected <T, R> void updateEntityFieldByValueIfNotNull(Object updateValue, T dbEntity, Function<T, R> getFunction, BiConsumer<T, R> setFunction, String field, ChangesHelper changesHelper) {
        if (updateValue == null) {
            return;
        }

        R dbValue = getFunction.apply(dbEntity);
        if (!changesHelper.isChanged(field, dbValue, updateValue)) {
            return;
        }

        setFunction.accept(dbEntity, (R) updateValue);
    }

    public <E, K> void load(Collection<E> srcCollection,
                                     Function<? super E, ? extends K> functionGetId,
                                     Function<? super E, UUID> functionGetGroupingId,
                                     Function<? super E, T> functionGetGroupingEntity,
                                     BiConsumer<E, T> functionSetGroupingEntity) throws ServiceException {
        if (srcCollection.isEmpty()) {
            return;
        }
        KitGrouped<E, K, UUID> needLoad = KitUtils.createNeedLoadGrouped(srcCollection, functionGetId, functionGetGroupingId, functionGetGroupingEntity);
        if (KitUtils.isEmpty(needLoad)) {
            return;
        }
        Kit<T, UUID> loaded = findEntitiesSafe(needLoad.getGroupedKeySet());
        UUID key;
        for (var item : needLoad) {
            key = functionGetGroupingId.apply(item);
            functionSetGroupingEntity.accept(item, loaded.get(key));
        }
    }

    public static <T, E extends Enum<E>> List<T> filterByEnum(Collection<T> items, Function<T, E> getter, E targetValue) {
        return items.stream()
                .filter(item -> getter.apply(item) == targetValue)
                .toList();
    }
}
