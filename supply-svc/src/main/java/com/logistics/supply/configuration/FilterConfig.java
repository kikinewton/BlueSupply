package com.logistics.supply.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    public FilterConfig(ObjectMapper objectMapper) {
        objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }
}
