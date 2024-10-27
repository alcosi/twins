package org.cambium.service;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Setter
@Service
@Slf4j
public class EntitySmartService {
    public String[] daoPackages = new String[]{};

    public <T> T save(UUID uuid, T entity, CrudRepository<T, UUID> repository, SaveMode mode) throws ServiceException {
        SaveResult<T> saveResult = saveWithResult(uuid, entity, repository, mode);
        return saveResult.getSavedEntity();
    }

    public <T> SaveResult<T> saveWithResult(UUID uuid, T entity, CrudRepository<T, UUID> repository, SaveMode mode) throws ServiceException {
        Optional<T> dbEntity;
        SaveResult<T> saveResult = new SaveResult<>();
        saveResult.setSavedEntity(entity);
        switch (mode) {
            case none:
                return saveResult;
            case ifNotPresentCreate:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isEmpty()) {
                    entity = repository.save(entity);
                    logSaving(uuid, entity);
                    saveResult
                            .setWasCreated(true)
                            .setSavedEntity(entity);
                    return saveResult;
                }
                return saveResult.setSavedEntity(dbEntity.get());
            case ifNotPresentThrows:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isEmpty())
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "unknown " + entityShortName(entity) + "[" + uuid + "]");
                return saveResult.setSavedEntity(dbEntity.get());
            case ifPresentThrows:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isPresent())
                    throw new ServiceException(ErrorCodeCommon.UUID_ALREADY_EXIST, entityShortName(entity) + "[" + uuid + "] is already exist");
                return saveResult;
            case ifPresentThrowsElseCreate:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isPresent())
                    throw new ServiceException(ErrorCodeCommon.UUID_ALREADY_EXIST, entityShortName(entity) + "[" + uuid + "] is already exist");
                else {
                    entity = repository.save(entity);
                    logSaving(uuid, entity);
                    saveResult
                            .setWasCreated(true)
                            .setSavedEntity(entity);
                    return saveResult;
                }
            case saveAndLogOnException:
                try {
                    entity = repository.save(entity);
                    logSaving(uuid, entity);
                    saveResult
                            .setWasSaved(true)
                            .setSavedEntity(entity);
                } catch (Exception exception) {
                    log.warn(entity.getClass().getSimpleName() + "[" + uuid + "] can not be saved: ", exception);
                }
                return saveResult;
            case saveAndThrowOnException:
                entity = repository.save(entity);
                logSaving(uuid, entity);
                saveResult
                        .setWasSaved(true)
                        .setSavedEntity(entity);
                return saveResult;
        }
        return saveResult;
    }

    @Data
    @Accessors(chain = true)
    public static class SaveResult<E> {
        private boolean wasCreated = false;
        private boolean wasSaved = false;
        private E savedEntity;
    }

    private UUID throwIfEmptyId(UUID uuid) throws ServiceException {
        if (uuid == null)
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "UUID can not be null");
        return uuid;
    }

    private <T> void logSaving(UUID uuid, T entity) {
        log.info(createSaveLogMsg(uuid, entity));
    }

    private <T> String createSaveLogMsg(UUID uuid, T entity) {
        if (entity instanceof EasyLoggable prettyLoggable)
            return prettyLoggable.easyLog(EasyLoggable.Level.DETAILED) + " was saved";
        else if (uuid != null)
            return entityShortName(entity) + " was saved. Perhaps with id[" + uuid + "]";
        else
            return entityShortName(entity) + " was saved. Please implement PrettyLoggable interface for more detailed logging";
    }

    public <T> T save(T entity, CrudRepository<T, UUID> repository, SaveMode mode) throws ServiceException {
        return save(null, entity, repository, mode);
    }

    private String entityShortName(Class entityClass) {
        return entityClass != null ? entityClass.getSimpleName().replaceAll("Entity", "") : "<unknown>";
    }

    private String entityShortName(Object entity) {
        return entityShortName(entity.getClass());
    }

    private <T> String entityShortName(CrudRepository<T, UUID> repository) {
        return entityShortName(getRepositoryEntityClass(repository));
    }

    private <T> Class<T> getRepositoryEntityClass(CrudRepository<T, UUID> repository) {
//        Type t = repository.getClass().getGenericSuperclass();
//        ParameterizedType pt = (ParameterizedType) t;
//        return (Class<T>) pt.getActualTypeArguments()[0];

        Type[] interfaces = repository.getClass().getInterfaces();

        for (Type t : interfaces) {
            if (t instanceof Class<?>) {
                Class<?> clazz = (Class<?>) t;
                for (String daoPackage : daoPackages) {
                    if (clazz.getPackage().getName().startsWith(daoPackage)) {
                        // Repositories should implement only ONE interface from application packages
                        Type genericInterface = clazz.getGenericInterfaces()[0];
                        return (Class<T>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
                    }
                }
            }
        }
        return null;
    }

    public static void entityReadDenied(ReadPermissionCheckMode readPermissionCheckMode, String logMessage) throws ServiceException {
        switch (readPermissionCheckMode) {
            case none:
                return;
            case ifDeniedLog:
                log.error(logMessage);
                return;
            case ifDeniedThrows:
                throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, logMessage);
        }
    }

    public enum SaveMode {
        ifNotPresentCreate,
        ifNotPresentThrows,
        none,
        ifPresentThrows,
        ifPresentThrowsElseCreate,
        saveAndLogOnException,
        saveAndThrowOnException
    }

    public <T> T findById(UUID uuid, CrudRepository<T, UUID> repository, FindMode mode) throws ServiceException {
        Optional<T> optional = repository.findById(uuid);
        switch (mode) {
            case ifEmptyNull:
                return optional.orElse(null);
            case ifEmptyLogAndNull:
                if (optional.isEmpty()) {
                    log.error(entityShortName(repository) + " can not find entity with id[" + uuid + "]");
                    return null;
                } else
                    return optional.get();
            case ifEmptyThrows:
                if (optional.isEmpty())
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, " unknown " + entityShortName(repository) + "[" + uuid + "]");
                return optional.get();
        }
        return null;
    }

    public <T> Kit<T, UUID> findByIdIn(Collection<UUID> ids, CrudRepository<T, UUID> repository, Function<T, UUID> functionGetId, ListFindMode mode) throws ServiceException {
        Iterable<T> iterable = repository.findAllById(ids);
        Kit<T, UUID> kit = new Kit<>(IterableUtils.toList(iterable), functionGetId);
        for (UUID id : ids) {
            if (!kit.containsKey(id)) {
                switch (mode) {
                    case ifMissedIgnore:
                        continue;
                    case ifMissedLog:
                        log.error(entityShortName(repository) + " can not find entity with id[" + id + "]");
                        continue;
                    case ifMissedThrows:
                            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "unknown " + entityShortName(repository) + "[" + id + "]");
                }
            }
        }
        return kit;
    }

    public enum FindMode {
        ifEmptyNull,
        ifEmptyLogAndNull,
        ifEmptyThrows,
    }

    public enum ListFindMode {
        ifMissedIgnore,
        ifMissedLog,
        ifMissedThrows,
    }

    public UUID check(UUID uuid, CrudRepository<?, UUID> repository, CheckMode checkMode) throws ServiceException {
        String entityName = entityShortName(repository);
        switch (checkMode) {
            case EMPTY:
                if (uuid != null)
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Incorrect " + entityName + "[" + uuid + "]");
                break;
            case EMPTY_OR_DB_EXISTS:
                if (uuid == null)
                    break;
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Unknown " + entityName + "[" + uuid + "]");
                break;
            case NOT_EMPTY:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Empty " + entityName);
                break;
            case NOT_EMPTY_AND_DB_EXISTS:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Empty " + entityName);
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Unknown " + entityName + "[" + uuid + "]");
                break;
            case NOT_EMPTY_AND_DB_MISSING:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Empty " + entityName);
                if (repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, entityName + "[" + uuid + "] is already present in database");
                break;
        }
        return uuid;
    }

    public UUID uuidFromString(String uuid, String fieldName) throws ServiceException {
        try {
            return UUID.fromString(uuid);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Incorrect " + fieldName + "[" + uuid + "]");
        }
    }

    public <T> void deleteAndLog(UUID uuid, CrudRepository<T, UUID> repository) throws ServiceException {
        repository.deleteById(throwIfEmptyId(uuid));
        log.info(entityShortName(repository) + "[" + uuid + "] perhaps was deleted");
    }

    public <T> void deleteAllAndLog(Iterable<UUID> uuidList, CrudRepository<T, UUID> repository) {
        repository.deleteAllById(uuidList);
        log.info("{}[{}] perhaps were deleted", entityShortName(repository), StringUtils.join(uuidList, ","));
    }

    public <T> void deleteAllEntitiesAndLog(Iterable<T> entities, CrudRepository<T, UUID> repository) {
        repository.deleteAll(entities);
        StringJoiner  entityLog = new StringJoiner(", ");
        for (T entity : entities) {
            if (entity instanceof EasyLoggable prettyLoggable)
                entityLog.add(prettyLoggable.logShort());
            else
                entityLog.add("<not loggable>");
        }
        log.info("{}[{}] perhaps were deleted", entityShortName(repository), entityLog);
    }

    public <T, K> Iterable<T> saveAllAndLog(Iterable<T> entities, CrudRepository<T, K> repository) {
        Iterable<T> result = repository.saveAll(entities);
        List<String> messages = new ArrayList<>();
        for (T e : result) {
            messages.add(createSaveLogMsg(null, e));
        }
        log.info(String.join(System.lineSeparator(), messages));
        return result;
    }

    public <T, K> Iterable<T> saveAllAndFlushAndLog(Iterable<T> entities, JpaRepository<T, K> repository) {
        Iterable<T> result  = repository.saveAllAndFlush(entities);
        List<String> messages = new ArrayList<>();
        for (T e : result) {
            messages.add(createSaveLogMsg(null, e));
        }
        log.info(String.join(System.lineSeparator(), messages));
        return result;
    }

    public <T> Iterable<T> saveAllAndLogChanges(Iterable<T> entities, CrudRepository<T, UUID> repository, ChangesHelper changesHelper) {
        Iterable<T> result = repository.saveAll(entities);
        log.info("Changes: " + changesHelper.collectForLog());
        return result;
    }

    public <T, K> Iterable<T>  saveAllAndLogChanges(Map<T, ChangesHelper> entityChangesMap, CrudRepository<T, K> repository) {
        return saveAllAndLog(entityChangesMap.keySet(), repository);
        //todo collect an log changes
    }

    public <T, K> Iterable<T>  saveAllAndFlushAndLogChanges(Map<T, ChangesHelper> entityChangesMap, JpaRepository<T, K> repository) {
        return saveAllAndFlushAndLog(entityChangesMap.keySet(), repository);
        //todo collect an log changes
    }

    public <T> T saveAndLogChanges(T entity, CrudRepository<T, UUID> repository, ChangesHelper changesHelper) {
        if (!changesHelper.hasChanges())
            return entity;
        entity = repository.save(entity);
        if (entity instanceof EasyLoggable prettyLoggable)
            log.info(prettyLoggable.easyLog(EasyLoggable.Level.SHORT) + " was updated: " + changesHelper.collectForLog());
        else
            log.info(entityShortName(entity) + " was updated: " + changesHelper.collectForLog());
        return entity;
    }

    public static <T> Map<UUID, T> convertToMap(List<T> entityList, Function<? super T, ? extends UUID> functionGetId) {
        Map<UUID, T> ret = null;
        if (entityList != null) {
            ret = entityList
                    .stream().collect(Collectors.toMap(functionGetId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        }
        return ret;
    }

    public enum CheckMode {
        NOT_EMPTY,
        NOT_EMPTY_AND_DB_EXISTS,
        NOT_EMPTY_AND_DB_MISSING,
        ANY,
        EMPTY,
        EMPTY_OR_DB_EXISTS
    }

    public enum ReadPermissionCheckMode {
        none,
        ifDeniedLog,
        ifDeniedThrows,
    }

    public enum EntityValidateMode {
        none,
        afterRead,
        beforeSave
    }

}
