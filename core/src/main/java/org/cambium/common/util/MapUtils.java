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
        Set<K> crossKeys = narrowSet(mainMap.keySet(), narrowMap.keySet());
        for (K key : crossKeys) {
            resultMap.put(key, narrowSet(mainMap.get(key), narrowMap.get(key)));
        }
        return resultMap;
    }
}
