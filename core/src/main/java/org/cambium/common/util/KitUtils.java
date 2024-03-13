package org.cambium.common.util;

import org.cambium.common.Kit;

public class KitUtils {
    public static boolean isEmpty(Kit<?> kit) {
        return kit == null || kit.isEmpty();
    }

    public static boolean isNotEmpty(Kit<?> kit) {
        return kit != null && kit.isNotEmpty();
    }
}
