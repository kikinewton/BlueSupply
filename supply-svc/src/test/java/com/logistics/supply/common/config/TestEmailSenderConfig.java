package com.logistics.supply.common.config;

import com.logistics.supply.email.EmailSender;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestEmailSenderConfig {

    @Bean
    public EmailSender emailSender() {
        return Mockito.mock(EmailSender.class);
    }
}
