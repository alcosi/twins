package org.cambium.common;

import lombok.Getter;

import java.util.*;

@Getter
public class CacheEvictCollector {
    Map<String, List<Object>> cacheEntries = new HashMap<>();

    public CacheEvictCollector add(String... cacheKeys) {
        for (String key : cacheKeys) {
            cacheEntries.put(key, Collections.emptyList());
        }
        return this;
    }

    public CacheEvictCollector add(Object cacheValue, String... cacheKeys) {
        for (String key : cacheKeys) {
            cacheEntries.computeIfAbsent(key, k -> new ArrayList<>()).add(cacheValue);
        }
        return this;
    }
}
