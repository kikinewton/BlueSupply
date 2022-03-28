package com.logistics.supply;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.logistics.supply.configuration.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;


@EnableAsync
@EnableCaching(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableConfigurationProperties({FileStorageProperties.class})

public class SupplyApplication {

  @CacheEvict(allEntries = true, value = "${bsupply.cache.names}")
  @Scheduled(fixedDelayString = "${bsupply.cache.flush.fixed.delay.milliseconds}")
  public void cacheEvict() {
  }

  @Bean
  public LoadingCache<String, String> loadingCache(){
    return CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
              @Override
              public String load(String s) throws Exception {
                return "";
              }
            });
  }

  public static void main(String[] args) {
    SpringApplication.run(SupplyApplication.class, args);
  }
}
