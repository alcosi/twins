package org.twins.core.service.system;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CacheUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.domain.system.CacheInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class CacheService {
    private final CacheManager cacheManager;

    public CacheInfo getCacheInfo(String cacheKey) throws ServiceException {
        Cache cache = cacheManager.getCache(cacheKey);

        if (cache == null) {
            throw new ServiceException(ErrorCodeCommon.CACHE_WRONG_KEY, "Cannot find cache by key: [" + cacheKey + "]");
        }

        long itemCount = 0;
        double sizeInMb = 0;

        if (cache instanceof CaffeineCache) {
            Object nativeCache = cache.getNativeCache();
            itemCount = ((com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache).estimatedSize();
            sizeInMb = (double) CacheUtils.estimateSize(cache) / (1024 * 1024);
        } else if (cache instanceof ConcurrentMapCache) {
            ConcurrentMap<?, ?> nativeCache = (ConcurrentMap<?, ?>) cache.getNativeCache();
            itemCount = nativeCache.size();
            sizeInMb = (double) CacheUtils.estimateSize(cache) / (1024 * 1024);
        } else {
            throw new ServiceException(ErrorCodeCommon.CACHE_TYPE_UNSUPPORTED, "Unsupported cache type: [" + cache.getClass().getSimpleName() + "]");
        }

        return new CacheInfo()
                .setCacheName(cacheKey)
                .setItemsCount(itemCount)
                .setSizeInMb(sizeInMb);
    }

    public void evictCacheRecord(String cacheKey, String recordKey) throws ServiceException {
        Cache cache = cacheManager.getCache(cacheKey);

        if (cache == null) {
            throw new ServiceException(ErrorCodeCommon.CACHE_WRONG_KEY, "Cannot find cache by key: [" + cacheKey + "]");
        }

        if (recordKey != null) {
            try {
                cache.evict(UUID.fromString(recordKey));
            } catch (IllegalArgumentException e) {
                cache.evict(recordKey);
            }
            log.info("Evicted record '{}' from cache '{}'", recordKey, cacheKey);
        } else {
            cache.clear();
            log.info("Cleared entire cache '{}'", cacheKey);
        }
    }

    public void evictAllCacheRecords() throws ServiceException {
        for (String cacheName : cacheManager.getCacheNames()) {
            evictCacheRecord(cacheName, null);
        }
    }

    /**
     * Retrieves information about all caches in the system.
     *
     * @return A list of CacheInfoDTO objects, each containing information about a specific cache.
     */
    public List<CacheInfo> getAllCachesInfo() {
        List<CacheInfo> result = new ArrayList<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                try {
                    CacheInfo cacheInfo = getCacheInfo(cacheName);
                    result.add(cacheInfo);
                } catch (ServiceException e) {
                    // Log the error but continue processing other caches
                    log.error("Error getting info for cache '{}': {}", cacheName, e.getMessage());
                }
            }
        }

        return result;
    }
}

