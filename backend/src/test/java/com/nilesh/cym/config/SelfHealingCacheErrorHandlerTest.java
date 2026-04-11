package com.nilesh.cym.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.serializer.SerializationException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SelfHealingCacheErrorHandlerTest {

    private final SelfHealingCacheErrorHandler handler = new SelfHealingCacheErrorHandler();

    @Test
    void handleCacheGetError_evictsBrokenEntry() {
        Cache cache = mock(Cache.class);

        handler.handleCacheGetError(new SerializationException("bad payload"), cache, "CAR");

        verify(cache).evict("CAR");
    }

    @Test
    void handleCacheGetError_clearsCacheWhenEvictFails() {
        Cache cache = mock(Cache.class);
        doThrow(new IllegalStateException("evict failed")).when(cache).evict("CAR");

        handler.handleCacheGetError(new SerializationException("bad payload"), cache, "CAR");

        verify(cache).evict("CAR");
        verify(cache).clear();
    }
}
