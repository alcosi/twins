package org.cambium.common.util;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.CacheEvictCollector;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.openjdk.jol.vm.VM;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class CacheUtils {

    public static <T> void evictCache(CacheManager cacheManager, @NotNull String cacheKey, T recordKey) throws ServiceException {
        if (null == cacheManager)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "Cache manager not property injected in service class.");
        Cache cache = cacheManager.getCache(cacheKey);
        if (cache != null) {
            if (null != recordKey) cache.evictIfPresent(recordKey);
            else log.info("Invalidating cache: " + cacheKey + " : " + cache.invalidate());
        }
    }

    public static <T> void evictCache(CacheManager cacheManager, @NonNull Map<String, List<T>> cacheEntries) throws ServiceException {
        if (cacheManager == null)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "Cache manager not properly injected in service class.");

        if (cacheEntries.isEmpty()) {
            log.warn("No cache entries provided for eviction.");
            return;
        }

        for (Map.Entry<String, List<T>> entry : cacheEntries.entrySet()) {
            String cacheKey = entry.getKey();
            List<T> recordKeys = entry.getValue();
            Cache cache = cacheManager.getCache(cacheKey);

            if (cache != null) {
                if (recordKeys != null && !recordKeys.isEmpty())
                    for (T recordKey : recordKeys)
                        cache.evictIfPresent(recordKey);
                else
                    log.info("Invalidating cache: " + cacheKey + " : " + cache.invalidate());

            }
        }
    }

    public static <T> void evictCache(CacheManager cacheManager, CacheEvictCollector cacheEvictCollector) throws ServiceException {
        evictCache(cacheManager, cacheEvictCollector.getCacheEntries());
    }
    public static long estimateSize(Cache cache) throws ServiceException {
        long totalSize = 0;

        if (cache instanceof CaffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = ((CaffeineCache) cache).getNativeCache();

            for (Object key : nativeCache.asMap().keySet()) {
                Object value = nativeCache.getIfPresent(key);
                totalSize += VM.current().sizeOf(value);
            }
        } else if (cache instanceof ConcurrentMapCache) {
            Object nativeCacheObj = cache.getNativeCache();
            ConcurrentMap<?, ?> nativeCache = (ConcurrentMap<?, ?>) nativeCacheObj;

            for (Object value : nativeCache.values()) {
                totalSize += VM.current().sizeOf(value);
            }
        } else {
            throw new ServiceException(ErrorCodeCommon.CACHE_TYPE_UNSUPPORTED, "Unsupported cache type: [" + cache.getClass().getSimpleName() + "]");
        }

        return totalSize;
    }
}
