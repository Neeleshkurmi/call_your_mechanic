package com.nilesh.cym.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.Nullable;

@Slf4j
public class SelfHealingCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("cache_get_failed cacheName={} key={} exceptionType={} message={}",
                cache.getName(),
                key,
                exception.getClass().getSimpleName(),
                exception.getMessage());
        evictCorruptEntry(cache, key, exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
        log.warn("cache_put_failed cacheName={} key={} exceptionType={} message={}",
                cache.getName(),
                key,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("cache_evict_failed cacheName={} key={} exceptionType={} message={}",
                cache.getName(),
                key,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("cache_clear_failed cacheName={} exceptionType={} message={}",
                cache.getName(),
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    private void evictCorruptEntry(Cache cache, Object key, RuntimeException originalException) {
        try {
            cache.evict(key);
            log.info("cache_entry_evicted cacheName={} key={} reason={}",
                    cache.getName(),
                    key,
                    originalException.getClass().getSimpleName());
        } catch (RuntimeException evictException) {
            log.warn("cache_entry_evict_failed cacheName={} key={} exceptionType={} message={}",
                    cache.getName(),
                    key,
                    evictException.getClass().getSimpleName(),
                    evictException.getMessage());

            try {
                cache.clear();
                log.info("cache_cleared_after_get_failure cacheName={} reason={}",
                        cache.getName(),
                        originalException.getClass().getSimpleName());
            } catch (RuntimeException clearException) {
                log.warn("cache_clear_after_get_failure_failed cacheName={} exceptionType={} message={}",
                        cache.getName(),
                        clearException.getClass().getSimpleName(),
                        clearException.getMessage());
            }
        }
    }
}
