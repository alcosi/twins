package org.cambium.common.util;

import java.util.UUID;

public class LTreeUtils {
    public static String matchInTheMiddle(UUID id) {
        return "*." + id.toString().replace("-", "_") + ".*";
    }

    public static String convertToLTreeFormat(UUID uuid) {
        return uuid.toString().replace("-", "_");
    }
}
