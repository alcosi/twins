package org.cambium.common.util;

import java.util.UUID;

public class UuidUtils {
    public static String toString(UUID uuid) {
        return uuid == null ? "" : uuid.toString();
    }
}
