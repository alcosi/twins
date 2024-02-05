package org.cambium.common.util;

import java.util.Map;

public class MapUtils extends org.apache.commons.collections.MapUtils {
    public static <K, T> T pullAny(Map<K, T> map) {
        if (isEmpty(map))
            return null;
        Map.Entry<K, T> entry = map.entrySet().iterator().next();
        T ret = entry.getValue();
        map.remove(entry.getKey());
        return ret;
    }
}
