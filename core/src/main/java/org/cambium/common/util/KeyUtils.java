package org.cambium.common.util;

import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;

public class KeyUtils {
    public static final String KEY_REGEXP = "^[a-zA-Z0-9_\\\\s]+$";

    public static String upperCaseNullFriendly(String key, ErrorCode errorCode) throws ServiceException {
        return formatKey(key, 3, 36, true, false, errorCode);
    }

    public static String upperCaseNullSafe(String key, ErrorCode errorCode) throws ServiceException {
        return formatKey(key, 3, 36, true, true, errorCode);
    }

    public static String lowerCaseNullFriendly(String key, ErrorCode errorCode) throws ServiceException {
        return formatKey(key, 3, 36, false, false, errorCode);
    }

    public static String lowerCaseNullSafe(String key, ErrorCode errorCode) throws ServiceException {
        return formatKey(key, 3, 36, false, true, errorCode);
    }

    public static String formatKey(String key, int minLength, int maxLength, boolean upperCaseElseLower, boolean exceptionOnNull, ErrorCode errorCode) throws ServiceException {
        if (key == null)
            if (exceptionOnNull)
                throw new ServiceException(errorCode, "empty");
            else
                return null; // for correct working with changesHelper
        if (key.length() < minLength || key.length() > maxLength)
            throw new ServiceException(errorCode, "key length incorrect");
        if (!key.matches(KEY_REGEXP))
            throw new ServiceException(errorCode, "invalid key format. Check by regexp: " + KEY_REGEXP);
        key = key.trim().replaceAll("\\s+", "_");
        if (upperCaseElseLower)
            key = key.toUpperCase();
        else
            key = key.toLowerCase();
        return key;
    }
}
