package org.cambium.common.util;

import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.twinlink.TwinLinkCUD;

public class CudUtils {
    public static boolean isEmpty(EntityCUD<?> entityCUD) {
        return entityCUD == null || entityCUD.isEmpty();
    }

    public static boolean isNotEmpty(EntityCUD<?> entityCUD) {
        return !isEmpty(entityCUD);
    }

    // overload for the composition-based TwinLinkCUD (does not extend EntityCUD — its create list
    // carries TwinLinkCreate wrappers, not plain entities)
    public static boolean isEmpty(TwinLinkCUD twinLinkCUD) {
        return twinLinkCUD == null || twinLinkCUD.isEmpty();
    }

    public static boolean isNotEmpty(TwinLinkCUD twinLinkCUD) {
        return !isEmpty(twinLinkCUD);
    }
}
