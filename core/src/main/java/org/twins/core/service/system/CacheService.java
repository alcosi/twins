package org.twins.core.service.system;

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

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

@Lazy
@Slf4j
@Service
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
        String cacheType = cache.getClass().getSimpleName();

        if (cache instanceof CaffeineCache) {
            Object nativeCache = cache.getNativeCache();
            itemCount = ((com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache).estimatedSize();
            sizeInMb = (double) CacheUtils.estimateSize(cache) / (1024 * 1024);
        } else if (cache instanceof ConcurrentMapCache) {
            ConcurrentMap<?, ?> nativeCache = (ConcurrentMap<?, ?>) cache.getNativeCache();
            itemCount = nativeCache.size();
            sizeInMb = (double) CacheUtils.estimateSize(cache) / (1024 * 1024);
        } else {
            throw new ServiceException(ErrorCodeCommon.CACHE_TYPE_UNSUPPORTED, "Unsupported cache type: [" + cacheType + "]");
        }

        return new CacheInfo()
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
}

