package com.logistics.supply.configuration;

import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Setter
@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

  @Value("${caching.expiration.time:30}")
  private int expirationDuration;

  @Bean
  @Primary
  @Override
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager() {
      @NonNull
      protected Cache createConcurrentMapCache(String name) {

        ConcurrentMap<Object, Object> map = CacheBuilder.newBuilder()
                .expireAfterWrite(expirationDuration, TimeUnit.SECONDS)
                .build()
                .asMap();

        return new ConcurrentMapCache(
            name,
                map,
            false);
      }
    };
  }

  @Bean
  public CacheManager rqCacheManager() {
    return new ConcurrentMapCacheManager() {
      @NonNull
      protected Cache createConcurrentMapCache(String name) {
        return new ConcurrentMapCache(
                name,
                CacheBuilder.newBuilder()
                        .expireAfterWrite(10, TimeUnit.SECONDS)
                        .build()
                        .asMap(),
                false);
      }
    };
  }
}
