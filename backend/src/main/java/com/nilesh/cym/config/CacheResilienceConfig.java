package com.nilesh.cym.config;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheResilienceConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SelfHealingCacheErrorHandler();
    }
}
