package org.cambium.common.util;

import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;

import java.util.Collection;
import java.util.function.Function;

public class KitUtils {
    public static boolean isEmpty(Kit<?, ?> kit) {
        return kit == null || kit.isEmpty();
    }

    public static boolean isNotEmpty(Kit<?, ?> kit) {
        return kit != null && kit.isNotEmpty();
    }

    public static <E, K, GK, GE> KitGrouped<E, K, GK> createNeedLoadGrouped(
            Collection<E> srcCollection, Function<? super E, ? extends K> functionGetId,
            Function<? super E, ? extends GK> functionGetGroupingId,
            Function<? super E, ? extends GE> functionGetGroupingObject) {
        KitGrouped<E, K, GK> needLoad = new KitGrouped<>(functionGetId, functionGetGroupingId);
        for (var item : srcCollection) {
            if (functionGetGroupingObject.apply(item) == null && functionGetGroupingId.apply(item) != null)
                needLoad.add(item);
        }
        return needLoad;
    }
}
