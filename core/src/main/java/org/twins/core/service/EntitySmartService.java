package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EntitySmartService {
    public <T> void create(UUID uuid, T entity, CrudRepository<T, UUID> repository, CreateMode mode) throws ServiceException {
        switch (mode) {
            case none:
                return;
            case ifNotPresentCreate:
                if (repository.findById(uuid).isEmpty())
                    repository.save(entity);
                return;
            case ifNotPresentThrows:
                if (repository.findById(uuid).isEmpty())
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown " + shortName(entity) + "[" + uuid + "]");
                return;
            case ifPresentThrows:
                if (repository.findById(uuid).isPresent())
                    throw new ServiceException(ErrorCodeTwins.UUID_ALREADY_EXIST, shortName(entity) + "[" + uuid + "] is already exist");
                return;
            case ifPresentThrowsElseCreate:
                if (repository.findById(uuid).isPresent())
                    throw new ServiceException(ErrorCodeTwins.UUID_ALREADY_EXIST, shortName(entity) + "[" + uuid + "] is already exist");
                else
                    repository.save(entity);
                return;
            case createIgnoreExists:
                try {
                    repository.save(entity);
                } catch (Exception exception) {
                    log.warn(entity.getClass().getSimpleName() + "[" + uuid + "] is already exist");
                }
                return;
            case createAndThrowsIfPresent:
                repository.save(entity);
                return;
        }
    }

    private String shortName(Object entity) {
        return entity.getClass().getSimpleName().replaceAll("Entity", "");
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

    public enum CreateMode {
        ifNotPresentCreate,
        ifNotPresentThrows,
        none,
        ifPresentThrows,
        ifPresentThrowsElseCreate,
        createIgnoreExists,
        createAndThrowsIfPresent
    }

    public <T> T findById(UUID uuid, String fieldName, CrudRepository<T, UUID> repository, FindMode mode) throws ServiceException {
        Optional<T> optional = repository.findById(uuid);
        switch (mode) {
            case ifEmptyNull:
                return optional.isEmpty() ? null : optional.get();
            case ifEmptyThrows:
                if (optional.isEmpty())
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown " + fieldName + "[" + uuid + "]");
                return optional.get();
        }
        return null;
    }

    public enum FindMode {
        ifEmptyNull,
        ifEmptyThrows,
    }

    public UUID check(String uuidStr, String fieldName, CrudRepository<?,UUID> repository, EntitySmartService.CheckMode checkMode) throws ServiceException {
        UUID uuid = null;
        switch (checkMode) {
            case EMPTY:
                if (StringUtils.isNotEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + fieldName + "[" + uuidStr + "]");
                break;
            case EMPTY_OR_DB_EXISTS:
                if (StringUtils.isEmpty(uuidStr))
                    break;
                uuid = uuidFromString(uuidStr, fieldName);
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Unknown " + fieldName + "[" + uuidStr + "]");
                break;
            case NOT_EMPTY:
                if (StringUtils.isEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + fieldName );
                uuidFromString(uuidStr, fieldName);
                break;
            case NOT_EMPTY_AND_DB_EXISTS:
                if (StringUtils.isEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + fieldName );
                uuid = uuidFromString(uuidStr, fieldName);
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Unknown " + fieldName + "[" + uuidStr + "]");
                break;
            case NOT_EMPTY_AND_DB_MISSING:
                if (StringUtils.isEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + fieldName );
                uuid = uuidFromString(uuidStr, fieldName);
                if (repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldName + "[" + uuidStr + "] is already present in database");
                break;
        }
        return uuid;
    }

    public UUID check(UUID uuid, String fieldName, CrudRepository<?,UUID> repository, EntitySmartService.CheckMode checkMode) throws ServiceException {
        switch (checkMode) {
            case EMPTY:
                if (uuid != null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + fieldName + "[" + uuid + "]");
                break;
            case EMPTY_OR_DB_EXISTS:
                if (uuid == null)
                    break;
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Unknown " + fieldName + "[" + uuid + "]");
                break;
            case NOT_EMPTY:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + fieldName );
                break;
            case NOT_EMPTY_AND_DB_EXISTS:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + fieldName );
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Unknown " + fieldName + "[" + uuid + "]");
                break;
            case NOT_EMPTY_AND_DB_MISSING:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Empty " + fieldName );
                if (repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldName + "[" + uuid + "] is already present in database");
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

}
