package com.nilesh.cym.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DevCacheProfileTest {

    @Test
    void devProfileUsesSimpleCacheManager() {
        try (var context = new SpringApplicationBuilder(DevCacheTestApplication.class)
                .profiles("dev")
                .web(WebApplicationType.NONE)
                .run()) {
            CacheManager cacheManager = context.getBean(CacheManager.class);
            assertInstanceOf(ConcurrentMapCacheManager.class, cacheManager);
        }
    }

    @SpringBootConfiguration
    @EnableCaching
    @Import(RedisConfig.class)
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class
    })
    static class DevCacheTestApplication {
    }
}
