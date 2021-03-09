package com.logistics.supply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching(proxyTargetClass = true)
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SupplyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupplyApplication.class, args);
	}

}
