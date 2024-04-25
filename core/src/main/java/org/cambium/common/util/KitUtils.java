package org.cambium.common.util;

import org.cambium.common.kit.Kit;

public class KitUtils {
    public static boolean isEmpty(Kit<?, ?> kit) {
        return kit == null || kit.isEmpty();
    }

    public static boolean isNotEmpty(Kit<?, ?> kit) {
        return kit != null && kit.isNotEmpty();
    }
}
