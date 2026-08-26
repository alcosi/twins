package org.cambium.common.util;

import org.twins.core.domain.EntityCUD;

public class CudUtils {
    public static boolean isEmpty(EntityCUD<?> entityCUD) {
        return entityCUD == null || entityCUD.isEmpty();
    }

    public static boolean isNotEmpty(EntityCUD<?> entityCUD) {
        return !isEmpty(entityCUD);
    }
}
