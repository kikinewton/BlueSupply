package com.logistics.supply.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class RequestLoggingFilterConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix(
                "REQUEST DATA : "
                        + " at "
                        + new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date())
                        + " ");
        return filter;
    }
}
