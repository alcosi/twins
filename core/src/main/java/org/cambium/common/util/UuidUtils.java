package org.cambium.common.util;

import java.util.UUID;
import java.util.regex.Pattern;

public class UuidUtils {
    public static String toString(UUID uuid) {
        return uuid == null ? "" : uuid.toString();
    }

    public static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isUUID(String uuidStr) {
        return UUID_REGEX.matcher(uuidStr).matches();
    }
}
