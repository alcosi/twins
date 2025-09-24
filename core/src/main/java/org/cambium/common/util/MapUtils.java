package org.cambium.common.util;

import java.util.HashMap;
import java.util.HashSet;
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
    
    public static <K> Map<K, Boolean> narrowMapOfBooleans(Map<K, Boolean> mainMap, Map<K, Boolean> narrowMap, Boolean mainMapPassFlag) {
        if (MapUtils.isNotEmpty(mainMap) && MapUtils.isNotEmpty(narrowMap)) {
            Map<K, Boolean> narrowedMap = new HashMap<>();
            Set<K> narrowedSet = narrowSet(mainMap.keySet(), narrowMap.keySet());
            for (var twinClassId : narrowedSet) {
                if (mainMap.get(twinClassId) && mainMapPassFlag.equals(narrowMap.get(twinClassId))) { //if main has pass flag, then we can narrow it
                    narrowedMap.put(twinClassId, false);
                } else {
                    narrowedMap.put(twinClassId, mainMap.get(twinClassId));
                }
            }
            return narrowedMap;
        } else if (MapUtils.isEmpty(mainMap)) {
            return narrowMap;
        } else {
            return mainMap;
        }
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

    public static <T, V> void safeAdd(Map<T, Set<V>> map, T key, V element) {
        if (element == null || key == null || map == null)
            return;
        map.computeIfAbsent(key, k -> new HashSet<>()).add(element);
    }
}
