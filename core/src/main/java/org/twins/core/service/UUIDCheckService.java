package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;

@Service
@Slf4j
public class UUIDCheckService {
    public UUID check(String uuidStr, String fieldName, CrudRepository<?,UUID> repository, CheckMode checkMode) throws ServiceException {
        UUID uuid = null;
        switch (checkMode) {
            case EMPTY:
                if (StringUtils.isNotEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Incorrect " + fieldName + "[" + uuidStr + "]");
                break;
            case EMPTY_OR_DB_EXISTS:
                if (StringUtils.isEmpty(uuidStr))
                    break;
                uuid = uuidFromString(uuidStr, fieldName);
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Unknown " + fieldName + "[" + uuidStr + "]");
                break;
            case NOT_EMPTY:
                if (StringUtils.isEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Empty " + fieldName );
                uuidFromString(uuidStr, fieldName);
                break;
            case NOT_EMPTY_AND_DB_EXISTS:
                if (StringUtils.isEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Empty " + fieldName );
                uuid = uuidFromString(uuidStr, fieldName);
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Unknown " + fieldName + "[" + uuidStr + "]");
                break;
            case NOT_EMPTY_AND_DB_MISSING:
                if (StringUtils.isEmpty(uuidStr))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Empty " + fieldName );
                uuid = uuidFromString(uuidStr, fieldName);
                if (repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, fieldName + "[" + uuidStr + "] is already present in database");
                break;
        }
        return uuid;
    }

    public UUID check(UUID uuid, String fieldName, CrudRepository<?,UUID> repository, CheckMode checkMode) throws ServiceException {
        switch (checkMode) {
            case EMPTY:
                if (uuid != null)
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Incorrect " + fieldName + "[" + uuid + "]");
                break;
            case EMPTY_OR_DB_EXISTS:
                if (uuid == null)
                    break;
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Unknown " + fieldName + "[" + uuid + "]");
                break;
            case NOT_EMPTY:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Empty " + fieldName );
                break;
            case NOT_EMPTY_AND_DB_EXISTS:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Empty " + fieldName );
                if (!repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Unknown " + fieldName + "[" + uuid + "]");
                break;
            case NOT_EMPTY_AND_DB_MISSING:
                if (uuid == null)
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Empty " + fieldName );
                if (repository.existsById(uuid))
                    throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, fieldName + "[" + uuid + "] is already present in database");
                break;
        }
        return uuid;
    }

    public UUID uuidFromString(String uuid, String fieldName) throws ServiceException {
        try {
            return UUID.fromString(uuid);
        } catch (Exception exception) {

            throw new ServiceException(ErrorCodeTwins.INCORRECT_UUID, "Incorrect " + fieldName + "[" + uuid + "]");
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
}
