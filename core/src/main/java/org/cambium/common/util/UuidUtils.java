package org.cambium.common.util;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.validation.constraints.NotNull;
import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
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

    public static UUID ifNullGenerate(UUID existingId) {
        return existingId != null ? existingId : UuidCreator.getTimeOrderedEpoch();
    }

    public static UUID generate() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    public static  <T> boolean hasValue(Collection<T> collection, @NotNull UUID value, Function<T, UUID> functionGetId) {
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        for (T item : collection) {
            if (value.equals(functionGetId.apply(item)))
                return true;
        }
        return false;
    }

    public static  <T> boolean hasValue(Collection<T> collection, String value, Function<T, UUID> functionGetId) {
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        return hasValue(collection, valueUUID, functionGetId);
    }

    public static <T> boolean hasNullifyMarker(Collection<T> collection, Function<T, UUID> functionGetId) {
        return hasValue(collection, NULLIFY_MARKER, functionGetId);
    }

    public static boolean equals(UUID id, String value) {
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        return valueUUID.equals(id);
    }
}
