package com.logistics.supply.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;


@Component
@RequiredArgsConstructor
public class SendEmailConfig {

//  @Value("${config.mail.template}")
//  String generalEmailTemplate;

  final SpringTemplateEngine templateEngine;

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }



}
