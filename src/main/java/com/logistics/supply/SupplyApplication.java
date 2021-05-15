package com.logistics.supply;

import com.logistics.supply.configuration.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableAsync
@EnableCaching(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableConfigurationProperties({FileStorageProperties.class})
public class SupplyApplication {

  public static void main(String[] args) {
    SpringApplication.run(SupplyApplication.class, args);
  }
}
