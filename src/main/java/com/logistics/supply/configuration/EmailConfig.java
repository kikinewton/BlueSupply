package com.logistics.supply.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Configuration
@EnableWebMvc
@ComponentScan({"com.logistics.supply"})
public class EmailConfig {

  @Value("${config.mail.template}")
  String FLOAT_ENDORSE_EMAIL;

  @Autowired SpringTemplateEngine templateEngine;


}
