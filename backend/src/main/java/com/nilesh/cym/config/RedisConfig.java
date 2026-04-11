package com.nilesh.cym.config;

import com.nilesh.cym.location.realtime.RedisLocationSubscriber;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    private static final String CACHE_PREFIX_VERSION = "cym:v6:";

    @Bean
    public MessageListenerAdapter locationEventListener(RedisLocationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter locationEventListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(locationEventListener, new PatternTopic("booking:*:location"));
        return container;
    }

    @Bean
    @Profile("!dev")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(GenericJacksonJsonRedisSerializer.builder()
                                .enableUnsafeDefaultTyping()
                                .typePropertyName("@class")
                                .build()))
                .disableCachingNullValues()
                // Bump the namespace when the cache serialization format changes so stale values do not break reads.
                .computePrefixWith(cacheName -> CACHE_PREFIX_VERSION + cacheName + "::")
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("services:list", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("services:detail", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("services:estimate", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("mechanics:detail", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("mechanics:reviews", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
