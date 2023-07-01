package com.logistics.supply.common.cache;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.service.RequestItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@IntegrationTest
class CacheableTests {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    RequestItemService requestItemService;

    @Test
     void testCacheableMethod() {
        // Ensure cache is empty
        assertCacheIsEmpty("requestItemsByDepartment");

        // First invocation
        requestItemService.getEndorsedRequestItemsForDepartment(10);

        // Verify that the result is cached
        assertCacheIsNotEmpty("requestItemsByDepartment");

        // Second invocation with the same parameter
//        requestItemService.getItemsWithFinalPriceUnderQuotation(100);
//
//        // Verify that the result is retrieved from the cache
//        assertCacheIsNotEmpty("itemsWithPriceByQuotationId");
    }

    private void assertCacheIsEmpty(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        assertEquals(0, getNativeCacheSize(cache));
    }

    private void assertCacheIsNotEmpty(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        assertNotEquals(0, getNativeCacheSize(cache));
    }

    private int getNativeCacheSize(Cache cache) {
        Object nativeCache = cache.getNativeCache();

        // Retrieve the size from the native cache implementation
        if (nativeCache instanceof ConcurrentMap) {
            return  ((AbstractMap<?,?>) nativeCache).size();
        }

        // Return 0 if the cache implementation is not ConcurrentMapCache
        return 0;
    }
}
