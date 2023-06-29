package com.logistics.supply.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.ResponseFilters;

import static org.zalando.logbook.core.Conditions.contentType;
import static org.zalando.logbook.core.HeaderFilters.authorization;
import static org.zalando.logbook.core.QueryFilters.accessToken;
import static org.zalando.logbook.core.QueryFilters.replaceQuery;


@Configuration
public class LogbookConfig {

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .responseFilter(ResponseFilters.replaceBody(message -> contentType("*/*-stream").test(message) ? "It just keeps going and going..." : null))
                .queryFilter(accessToken())
                .queryFilter(replaceQuery("password", "<secret>"))
                .headerFilter(authorization())
                .build();
    }

}
