package com.nilesh.cym.catalog.service;

import com.nilesh.cym.config.CacheResilienceConfig;
import com.nilesh.cym.entity.ServiceEntity;
import com.nilesh.cym.entity.enums.VehicleType;
import com.nilesh.cym.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ServiceCatalogCachingTest.TestConfig.class,
        CacheResilienceConfig.class
})
class ServiceCatalogCachingTest {

    @jakarta.annotation.Resource
    private ServiceCatalogService serviceCatalogService;

    @jakarta.annotation.Resource
    private ServiceRepository serviceRepository;

    @jakarta.annotation.Resource
    private CorruptibleCacheManager cacheManager;

    @Test
    void findServices_recoversFromCorruptCacheAndRepopulates() {
        ServiceEntity first = buildService(1L, "Flat Tyre Repair", VehicleType.CAR, 299D);
        ServiceEntity second = buildService(2L, "Battery Jump Start", VehicleType.CAR, 499D);
        when(serviceRepository.findByVehicleType(VehicleType.CAR))
                .thenReturn(List.of(first))
                .thenReturn(List.of(second));

        List<?> firstResponse = serviceCatalogService.findServices(VehicleType.CAR);
        assertEquals(1, firstResponse.size());

        cacheManager.corrupt("services:list", "CAR");

        List<?> secondResponse = serviceCatalogService.findServices(VehicleType.CAR);
        assertEquals(1, secondResponse.size());
        assertEquals("Battery Jump Start", ((com.nilesh.cym.catalog.dto.ServiceResponseDto) secondResponse.get(0)).name());

        List<?> thirdResponse = serviceCatalogService.findServices(VehicleType.CAR);
        assertEquals("Battery Jump Start", ((com.nilesh.cym.catalog.dto.ServiceResponseDto) thirdResponse.get(0)).name());
        Mockito.verify(serviceRepository, times(2)).findByVehicleType(VehicleType.CAR);
    }

    private static ServiceEntity buildService(Long id, String name, VehicleType vehicleType, Double charge) {
        ServiceEntity entity = new ServiceEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(name + " service");
        entity.setVehicleType(vehicleType);
        entity.setServiceCharge(charge);
        return entity;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        ServiceRepository serviceRepository() {
            return Mockito.mock(ServiceRepository.class);
        }

        @Bean
        ServiceCatalogService serviceCatalogService(ServiceRepository serviceRepository) {
            return new ServiceCatalogService(serviceRepository);
        }
        @Bean
        CorruptibleCacheManager cacheManager() {
            CorruptibleCacheManager cacheManager = new CorruptibleCacheManager();
            cacheManager.setCaches(List.of(new CorruptibleCache("services:list")));
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
