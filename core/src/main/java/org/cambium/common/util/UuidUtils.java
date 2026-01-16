package org.cambium.common.util;

import com.github.f4b6a3.uuid.UuidCreator;
import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;
import java.util.regex.Pattern;

public class UuidUtils {
    public static final UUID NULLIFY_MARKER = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public static String toString(UUID uuid) {
        return uuid == null ? "" : uuid.toString();
    }

    public static UUID fromString(String uuidStr) throws ServiceException {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "incorrect UUID[" + uuidStr + "]");
        }
        return uuid;
    }

    public static UUID fromStringOrNull(String uuidStr) throws ServiceException {
        if (StringUtils.isEmpty(uuidStr)) {
            return null;
        }
        return fromString(uuidStr);
    }

    public static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isUUID(String uuidStr) {
        return UUID_REGEX.matcher(uuidStr).matches();
    }

    public static UUID nullifyIfNecessary(UUID uuid) {
        return NULLIFY_MARKER.equals(uuid) ? null : uuid;
    }

    public static boolean isNullifyMarker(UUID uuid) {
        return NULLIFY_MARKER.equals(uuid);
    }

    public static boolean isNullifyMarker(String stringUuid) throws ServiceException {
        return NULLIFY_MARKER.equals(fromString(stringUuid));
    }

    public static UUID ifNullGenerate(UUID existingId) {
        return existingId != null ? existingId : UuidCreator.getTimeOrderedEpoch();
    }

    public static UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
