package org.cambium.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.cambium.common.util.SetUtils.narrowSet;

public class MapUtils extends org.apache.commons.collections.MapUtils {
    public static <K, T> T pullAny(Map<K, T> map) {
        if (isEmpty(map))
            return null;
        Map.Entry<K, T> entry = map.entrySet().iterator().next();
        T ret = entry.getValue();
        map.remove(entry.getKey());
        return ret;
    }

    public static <K, V> Map<K, Set<V>> narrowMapOfSets(Map<K, Set<V>> mainMap, Map<K, Set<V>> narrowMap) {
        if (narrowMap == null) return mainMap;
        Map<K, Set<V>> resultMap = new HashMap<>();
        for (Map.Entry<K, Set<V>> entry : narrowMap.entrySet()) {
            K key = entry.getKey();
            Set<V> values = entry.getValue();
            if (mainMap.containsKey(key)) {
                resultMap.put(key, narrowSet(mainMap.get(key), values));
            } else {
                resultMap.put(key, values);
            }
        }
        return resultMap;
    }

    public static int sizeOf(Map<?, ?> map) {
        if (map == null) return 0;
        return map.size();
    }

    public static boolean areEqual(Map<String, String> first, Map<String, String> second) {
        if (first == second && first == null)
            return true;
        if (sizeOf(first) != sizeOf(second)) {
            return false;
        }
        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }
}
