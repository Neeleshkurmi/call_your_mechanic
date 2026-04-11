package com.nilesh.cym.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

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
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    static class DevCacheTestApplication {
    }
}
