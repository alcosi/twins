package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.exception.ErrorCodeTwins;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EntitySmartService {
    public static final String DAO_BASE_PACKAGE = "org.twins.core.dao";
    public <T> T save(UUID uuid, T entity, CrudRepository<T, UUID> repository, SaveMode mode) throws ServiceException {
        Optional<T> dbEntity;
        switch (mode) {
            case none:
                return entity;
            case ifNotPresentCreate:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isEmpty()) {
                    entity = repository.save(entity);
                    logSaving(uuid, entity);
                    return entity;
                }
                return dbEntity.get();
            case ifNotPresentThrows:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isEmpty())
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown " + entityShortName(entity) + "[" + uuid + "]");
                return dbEntity.get();
            case ifPresentThrows:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isPresent())
                    throw new ServiceException(ErrorCodeTwins.UUID_ALREADY_EXIST, entityShortName(entity) + "[" + uuid + "] is already exist");
                return entity;
            case ifPresentThrowsElseCreate:
                dbEntity = repository.findById(throwIfEmptyId(uuid));
                if (dbEntity.isPresent())
                    throw new ServiceException(ErrorCodeTwins.UUID_ALREADY_EXIST, entityShortName(entity) + "[" + uuid + "] is already exist");
                else {
                    entity = repository.save(entity);
                    logSaving(uuid, entity);
                }
                return entity;
            case saveAndLogOnException:
                try {
                    entity = repository.save(entity);
                    logSaving(uuid, entity);
                } catch (Exception exception) {
                    log.warn(entity.getClass().getSimpleName() + "[" + uuid + "] can not be saved: ", exception);
                }
                return entity;
            case saveAndThrowOnException:
                entity = repository.save(entity);
                logSaving(uuid, entity);
                return entity;
        }
        return entity;
    }

    private UUID throwIfEmptyId(UUID uuid) throws ServiceException {
        if (uuid == null)
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "UUID can not be null");
        return uuid;
    }

    private <T> void logSaving(UUID uuid, T entity) {
        if (entity instanceof EasyLoggable prettyLoggable)
            log.info(prettyLoggable.easyLog(EasyLoggable.Level.DETAILED) + " was saved");
        else if (uuid != null)
            log.info(entityShortName(entity) + " was saved. Perhaps with id[" + uuid + "]");
        else
            log.info(entityShortName(entity) + " was saved. Please implement PrettyLoggable interface for more detailed logging");
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
                if (clazz.getPackage().getName().startsWith(DAO_BASE_PACKAGE)) {
                    // Repositories should implement only ONE interface from application packages
                    Type genericInterface = clazz.getGenericInterfaces()[0];
                    return (Class<T>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
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
                throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, logMessage);
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
                return optional.isEmpty() ? null : optional.get();
            case ifEmptyThrows:
                if (optional.isEmpty())
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown " + entityShortName(repository) + "[" + uuid + "]");
                return optional.get();
        }
        return null;
    }

    public enum FindMode {
        ifEmptyNull,
        ifEmptyThrows,
    }
    public UUID check(UUID uuid, CrudRepository<?, UUID> repository, CheckMode checkMode) throws ServiceException {
        String entityName = entityShortName(repository);
        switch (checkMode) {
            case EMPTY:
                if (uuid != null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + entityName + "[" + uuid + "]");
                break;
            case EMPTY_OR_DB_EXISTS:
                if (uuid == null)
                    break;
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Unknown " + entityName + "[" + uuid + "]");
                break;
            case NOT_EMPTY:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + entityName);
                break;
            case NOT_EMPTY_AND_DB_EXISTS:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + entityName);
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Unknown " + entityName + "[" + uuid + "]");
                break;
            case NOT_EMPTY_AND_DB_MISSING:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + entityName);
                if (repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, entityName + "[" + uuid + "] is already present in database");
                break;
        }
        return uuid;
    }

    public UUID uuidFromString(String uuid, String fieldName) throws ServiceException {
        try {
            return UUID.fromString(uuid);
        } catch (Exception exception) {
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + fieldName + "[" + uuid + "]");
        }
    }

    public <T> void deleteAndLog(UUID uuid, CrudRepository<T, UUID> repository) throws ServiceException {
        repository.deleteById(throwIfEmptyId(uuid));
        log.info(entityShortName(repository) + " perhaps was deleted");
    }

    public <T> Iterable<T> saveAllAndLog(Iterable<T> entities, CrudRepository<T, UUID> repository) {
        Iterable<T> result = repository.saveAll(entities);
        result.forEach(e -> logSaving(null, e));
        return result;
    }

    public <T> Iterable<T> saveAllAndLogChanges(Iterable<T> entities, CrudRepository<T, UUID> repository, ChangesHelper changesHelper) {
        Iterable<T> result = saveAllAndLog(entities, repository);
        log.info("Changes: " + changesHelper.collectForLog());
        return result;
    }

    public <T> T saveAndLogChanges(T entity, CrudRepository<T, UUID> repository, ChangesHelper changesHelper) {
        if (!changesHelper.hasChanges())
            return entity;
        entity = repository.save(entity);
        if (entity instanceof EasyLoggable prettyLoggable)
            log.info(prettyLoggable.easyLog(EasyLoggable.Level.DETAILED) + " was updated: " + changesHelper.collectForLog());
        else
            log.info(entityShortName(entity) + " was updated: " + changesHelper.collectForLog());
        return entity;
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
