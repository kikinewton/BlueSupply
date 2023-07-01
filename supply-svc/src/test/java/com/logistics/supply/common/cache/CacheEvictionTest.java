package com.logistics.supply.common.cache;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.service.RequestItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@IntegrationTest
 class CacheEvictionTest {

    @Autowired
     CacheManager cacheManager;

    @Autowired
     RequestItemService requestItemService;

    @Test
     void testCacheEviction() {
        // Obtain the cache by name
        Cache requestItemsCache = cacheManager.getCache("requestItemsByDepartment");

        // Store a value in the cache
        String cacheKey = "department1";
        assert requestItemsCache != null;
        requestItemsCache.put(cacheKey, "cached value");

        // Verify the value is present in the cache
        String cachedValue = requestItemsCache.get(cacheKey, String.class);
        assertNotNull(cachedValue, "Value should be present in the cache");

        // Invoke the method that triggers cache eviction
        requestItemService.resolveCommentOnRequest(100);

        // Verify that the value is evicted from the cache
        cachedValue = requestItemsCache.get(cacheKey, String.class);
        assertNull(cachedValue, "Value should be evicted from the cache");
    }
}
