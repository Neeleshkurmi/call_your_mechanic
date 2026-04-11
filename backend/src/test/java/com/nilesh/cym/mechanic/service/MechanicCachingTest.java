package com.nilesh.cym.mechanic.service;

import com.nilesh.cym.config.CacheResilienceConfig;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicLocationRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.JwtService;
import com.nilesh.cym.token.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        MechanicCachingTest.TestConfig.class,
        CacheResilienceConfig.class
})
class MechanicCachingTest {

    @jakarta.annotation.Resource
    private MechanicService mechanicService;

    @jakarta.annotation.Resource
    private MechanicRepository mechanicRepository;

    @jakarta.annotation.Resource
    private CorruptibleCacheManager cacheManager;

    @Test
    void getMechanicDetails_recoversFromCorruptCacheAndRepopulates() {
        MechanicEntity first = buildMechanic(5L, "First Mechanic");
        MechanicEntity second = buildMechanic(5L, "Updated Mechanic");
        when(mechanicRepository.findWithUserById(5L))
                .thenReturn(Optional.of(first))
                .thenReturn(Optional.of(second));

        assertEquals("First Mechanic", mechanicService.getMechanicDetails(5L).name());

        cacheManager.corrupt("mechanics:detail", 5L);

        assertEquals("Updated Mechanic", mechanicService.getMechanicDetails(5L).name());
        assertEquals("Updated Mechanic", mechanicService.getMechanicDetails(5L).name());
        Mockito.verify(mechanicRepository, times(2)).findWithUserById(5L);
    }

    private static MechanicEntity buildMechanic(Long id, String name) {
        UserEntity user = new UserEntity();
        user.setId(8L);
        user.setName(name);
        user.setMob("+919900000001");

        MechanicEntity mechanic = new MechanicEntity();
        mechanic.setId(id);
        mechanic.setUser(user);
        mechanic.setAvailable(Boolean.TRUE);
        mechanic.setExperienceYears(4);
        mechanic.setRating(BigDecimal.valueOf(4.7));
        mechanic.setSkills("Diagnostics");
        mechanic.setBio("Fast roadside support");
        return mechanic;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        MechanicRepository mechanicRepository() {
            return Mockito.mock(MechanicRepository.class);
        }

        @Bean
        MechanicLocationRepository mechanicLocationRepository() {
            return Mockito.mock(MechanicLocationRepository.class);
        }

        @Bean
        BookingRepository bookingRepository() {
            return Mockito.mock(BookingRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        RefreshTokenRepository refreshTokenRepository() {
            return Mockito.mock(RefreshTokenRepository.class);
        }

        @Bean
        MechanicService mechanicService(
                MechanicRepository mechanicRepository,
                MechanicLocationRepository mechanicLocationRepository,
                BookingRepository bookingRepository,
                UserRepository userRepository,
                JwtService jwtService,
                RefreshTokenRepository refreshTokenRepository
        ) {
            return new MechanicService(
                    mechanicRepository,
                    mechanicLocationRepository,
                    bookingRepository,
                    userRepository,
                    jwtService,
                    refreshTokenRepository
            );
        }
        @Bean
        CorruptibleCacheManager cacheManager() {
            CorruptibleCacheManager cacheManager = new CorruptibleCacheManager();
            cacheManager.setCaches(List.of(new CorruptibleCache("mechanics:detail")));
            return cacheManager;
        }
    }

    static class CorruptibleCacheManager extends SimpleCacheManager {

        void corrupt(String cacheName, Object key) {
            ((CorruptibleCache) getCache(cacheName)).corrupt(key);
        }
    }

    static class CorruptibleCache extends ConcurrentMapCache {

        private final Set<Object> corruptKeys = ConcurrentHashMap.newKeySet();

        CorruptibleCache(String name) {
            super(name);
        }

        void corrupt(Object key) {
            corruptKeys.add(key);
        }

        @Override
        public ValueWrapper get(Object key) {
            if (corruptKeys.remove(key)) {
                throw new org.springframework.data.redis.serializer.SerializationException("corrupt cache value");
            }
            return super.get(key);
        }
    }
}
