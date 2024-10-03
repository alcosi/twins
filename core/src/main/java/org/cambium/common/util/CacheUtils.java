package org.cambium.common.util;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Slf4j
public class CacheUtils {

    public static <T> void evictCache(CacheManager cacheManager, String cacheKey, T recordKey) throws ServiceException {
        if (null != cacheManager) {
            Cache cache = cacheManager.getCache(cacheKey);
            if (cache != null) {
                if (null != recordKey) cache.evictIfPresent(recordKey.toString());
                else log.info("INVALIDATING CACHE: " + cacheKey + " : " + cache.invalidate());
            }
        } else {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "Cache manager not property injected in service class.");
        }
    }
}
